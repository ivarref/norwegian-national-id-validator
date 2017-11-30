# Norwegian national ID validator written in Clojure/Script

Validate Norwegian national identity numbers ([birth number](https://en.wikipedia.org/wiki/National_identification_number#Norway) ([fødselsnummer](https://no.wikipedia.org/wiki/F%C3%B8dselsnummer)), D-number, H-number and FH-number).

Compiles to both Clojure and ClojureScript. No external dependencies.

## Installation

[![Clojars Project](http://clojars.org/norwegian-national-id-validator/latest-version.svg)](http://clojars.org/norwegian-national-id-validator)

Add `[norwegian-national-id-validator "0.1.1"]` to your dependency vector.


## Usage

Add `[norwegian-national-id-validator.core :refer [validate-norwegian-id-number]]`
to your require section.

```clojure
(validate-norwegian-id-number "29029900157")
=> false
    
(validate-norwegian-id-number "29029600013")
=> true
```

## Example usage with prismatic/schema

```clojure
(ns user.nin-schema
  (:require [norwegian-national-id-validator.core :as nin-validator]
            [schema.spec.core :as spec]
            [schema.spec.leaf :as leaf]
            [clojure.test :as test]
            [schema.core :as s])
  (:import (schema.core Schema)))

(clojure.core/defrecord NinSchema []
  Schema
  (spec [this] (leaf/leaf-spec (spec/simple-precondition this nin-validator/validate-norwegian-id-number)))
  (explain [this] (list 'validate-norwegian-id-number)))
  
  
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
