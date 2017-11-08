(ns norwegian-national-id-validator.cljs-runner
  (:require [doo.runner :refer-macros [doo-tests]]
            [norwegian-national-id-validator.core-test]))

(doo-tests 'norwegian-national-id-validator.core-test)