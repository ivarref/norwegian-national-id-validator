(ns norwegian-national-id-validator.core-test
  (:require #?(:clj  [clojure.test :refer [testing deftest is]]
               :cljs [cljs.test :refer-macros [deftest is testing]])
            [norwegian-national-id-validator.core :refer [norwegian-id-number? valider-id]]
            [norwegian-national-id-validator.nintestdata :as nintestdata]))

(defmacro try-data [form]
  `(try ~form
        (catch #?(:clj Exception :cljs js/Error) e# (ex-data e#))))

; ported from https://github.com/mikaello/norwegian-national-id-validator/blob/master/__test__/index.test.js

(deftest nin-validator
  (testing "works for valid birth numbers for men born on 1. Jan 1901"
    (doseq [nin (-> nintestdata/data :01-01-1901 :men)]
      (is (true? (norwegian-id-number? nin)))))

  (testing "works for valid birth numbers for women born on 1. Jan 1901"
    (doseq [nin (-> nintestdata/data :01-01-1901 :women)]
      (is (true? (norwegian-id-number? nin)))))

  (testing "works with D numbers"
    (doseq [nin (-> nintestdata/data :DNumbers)]
      (is (true? (norwegian-id-number? nin)))))

  (testing "knows that no one could possibly be born on 29. Feb 1999"
    (is (false? (norwegian-id-number? "29029900157"))))

  (testing "knows that it is possible to be born on 29. Feb 1996"
    (is (true? (norwegian-id-number? "29029600013"))))

  (testing "does not accept future valid ID numbers"
    (is (false? (norwegian-id-number? "24088951559"))))

  (testing "does not accept invalid dates"
    (is (:dato (try-data (valider-id "81234567803" false))))
    (is (:dato (try-data (valider-id "01415612381" false)))))


  (testing "does not accept ID numbers with invalid check digits"
    (is (:gitt-nummer (try-data (valider-id "03119975255" false))))
    (is (:gitt-nummer (try-data (valider-id "67047000658" false)))))

  (testing "spaces are treated as error"
    (is (false? (norwegian-id-number? " 81234567802")))
    (is (false? (norwegian-id-number? "91234567883 "))))


  (testing "empty string should be treated as invalid national id number"
    (is (false? (norwegian-id-number? ""))))

  (testing "handle non-string input"
    (is (false? (norwegian-id-number? nil)))
    (is (false? (norwegian-id-number? 13922947702)))
    (is (false? (norwegian-id-number? ["Hello" "World"]))))

(deftest synthetic-support
  (is (true? (norwegian-id-number? "12812199631")))
  (is (true? (norwegian-id-number? "10884399853")))
  (is (true? (norwegian-id-number? "13922947702")))
  (is (false? (norwegian-id-number? "13932947702")))))
