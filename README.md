# Norwegian national ID validator written in Clojure/Script

Validate Norwegian national identity numbers ([birth number](https://en.wikipedia.org/wiki/National_identification_number#Norway) ([fødselsnummer](https://no.wikipedia.org/wiki/F%C3%B8dselsnummer)) and D-number.

Compiles to both Clojure and ClojureScript. No external dependencies.

## Installation

[![Clojars Project](https://img.shields.io/clojars/v/com.github.ivarref/norwegian-national-id-validator.svg)](https://clojars.org/com.github.ivarref/norwegian-national-id-validator)

## Usage

Add `[norwegian-national-id-validator.core :refer [norwegian-id-number?]]`
to your require section.

```clojure
(norwegian-id-number? "29029900157")
=> false
    
(norwegian-id-number? "29029600013")
=> true

; synthetic ID numbers, as per https://docs.digdir.no/docs/idporten/idporten/idporten_testbrukere#testid, is also supported:
(norwegian-id-number? "13922947702")
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

## Making a new release

Go to [./actions/workflows/release.yml](https://github.com/ivarref/norwegian-national-id-validator/actions/workflows/release.yml)
and press `Run workflow`.

## Credits

This project was initially a port of [mikaello's national id validator](https://github.com/mikaello/norwegian-national-id-validator) (for Node).

Migrated to a port of Skatteetaten's code:

* https://skatteetaten.github.io/folkeregisteret-api-dokumentasjon/nytt-fodselsnummer-fra-2032/
* https://skatteetaten.github.io/folkeregisteret-api-dokumentasjon/dokumenter/foedselsEllerDNummerValidator.java
* https://skatteetaten.github.io/folkeregisteret-api-dokumentasjon/dokumenter/foedselsEllerDNummerValidator.js

## Change log

#### [0.2.x] 2026-01-09

* Support
  for [2032-style birth numbers](https://skatteetaten.github.io/folkeregisteret-api-dokumentasjon/nytt-fodselsnummer-fra-2032/).
* Remove support for H-number and FH-number.

#### [0.1.x] 2017 – 2026-01-08

Initial project supporting birth number validation.

## License

Copyright © 2017 – 2026 Ivar Refsdal

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.

