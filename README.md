# Norwegian national ID validator written in Clojure/Script

A Norwegian national ID validator that compiles to both Clojure and ClojureScript.
No external dependencies.

## Installation

[![Clojars Project](http://clojars.org/norwegian-national-id-validator/latest-version.svg)](http://clojars.org/norwegian-national-id-validator)

## Usage

    $ lein repl
    
    (use 'norwegian-national-id-validator.core)
              
    (validate-norwegian-id-number "29029900157")
    => false
    
    (validate-norwegian-id-number "29029600013")
    => true

## Tests

    lein test

## Credits

This project is a port of [mikaello's national id validator](https://github.com/mikaello/norwegian-national-id-validator) (for Node).

## License

Copyright © 2017 FIXME

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
