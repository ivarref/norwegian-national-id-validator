# Norwegian national ID validator written in Clojure/Script

Validate Norwegian national identity numbers ([birth number](https://en.wikipedia.org/wiki/National_identification_number#Norway) ([fødselsnummer](https://no.wikipedia.org/wiki/F%C3%B8dselsnummer)), D-number, H-number and FH-number).

Compiles to both Clojure and ClojureScript. No external dependencies.

## Installation

[![Clojars Project](https://img.shields.io/clojars/v/com.github.ivarref/norwegian-national-id-validator.svg)](https://clojars.org/com.github.ivarref/norwegian-national-id-validator)

## Usage

Add `[com.github.ivarref.norwegian-national-id-validator :refer [norwegian-id-number?]]`
to your require section.

```clojure
(norwegian-id-number? "29029900157")
=> false
    
(norwegian-id-number? "29029600013")
=> true

; synthetic ID numbers, as per https://docs.digdir.no/docs/idporten/idporten/idporten_testbrukere#testid, is also supported:
(norwegian-id-number? "29029600013")
=> true
```

## Example usage with prismatic/schema

```clojure
(ns user.nin-schema
  (:require [com.github.ivarref.norwegian-national-id-validator :as nin-validator]
            [schema.spec.core :as spec]
            [schema.spec.leaf :as leaf]
            [clojure.test :as test]
            [schema.core :as s])
  (:import (schema.core Schema)))

(clojure.core/defrecord NinSchema []
  Schema
  (spec [this] (leaf/leaf-spec (spec/simple-precondition this nin-validator/norwegian-id-number?)))
  (explain [this] (list 'norwegian-id-number?)))
  
  
(ns user.my-other-ns
  (:require [user.nin-schema :as nin-schema])
  (:import (user.nin_schema NinSchema)))

; (test/is (= {:nin "10101097000"} (s/validate {:nin (NinSchema.)} {:nin "10101097000"})))
; (test/is (ex-data (s/validate {:nin (NinSchema.)} {:nin "12345678901"})))
```

## Tests

    lein test

## Credits

This project is a port of [mikaello's national id validator](https://github.com/mikaello/norwegian-national-id-validator) (for Node).

## License

Copyright © 2017 Ivar Refsdal

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
