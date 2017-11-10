(ns norwegian-national-id-validator.core-test
  (:require #?(:clj [clojure.test :refer :all]
               :cljs [cljs.test :refer-macros [deftest is testing]])
                    [norwegian-national-id-validator.core :refer [validateNorwegianIdNumber-exdata possibleAgesOfPersonWithIdNumber]]
                    [norwegian-national-id-validator.nintestdata :as nintestdata]))

; ported from https://github.com/mikaello/norwegian-national-id-validator/blob/master/__test__/index.test.js

(deftest nin-validator
  (testing "works for valid birth numbers for men born on 1. Jan 1901"
    (doseq [nin (-> nintestdata/data :01-01-1901 :men)]
      (is (nil? (ex-data (validateNorwegianIdNumber-exdata nin))))))

  (testing "works for valid birth numbers for women born on 1. Jan 1901"
    (doseq [nin (-> nintestdata/data :01-01-1901 :women)]
      (is (nil? (ex-data (validateNorwegianIdNumber-exdata nin))))))

  (testing "works with D numbers"
    (doseq [nin (-> nintestdata/data :DNumbers)]
      (is (nil? (ex-data (validateNorwegianIdNumber-exdata nin))))))

  (testing "knows that no one could possibly be born on 29. Feb 1999"
    (is (ex-data (validateNorwegianIdNumber-exdata "29029900157"))))

  (testing "knows that it is possible to be born on 29. Feb 1996"
    (is (nil? (ex-data (validateNorwegianIdNumber-exdata "29029600013")))))

  (testing "does not accept future valid ID numbers"
    (is (ex-data (validateNorwegianIdNumber-exdata "24088951559")))) ; TODO I think this is incorrect

  (testing "does not accept ID numbers with invalid check digits"
    (is (= :digitcheck (:code (ex-data (validateNorwegianIdNumber-exdata "81234567803")))))
    (is (= :digitcheck (:code (ex-data (validateNorwegianIdNumber-exdata "01415612381")))))
    (is (= :digitcheck (:code (ex-data (validateNorwegianIdNumber-exdata "03119975255")))))
    (is (= :digitcheck (:code (ex-data (validateNorwegianIdNumber-exdata "67047000658"))))))

  (testing "works with FH numbers"
    (is (nil? (ex-data (validateNorwegianIdNumber-exdata "81234567802"))))
    (is (nil? (ex-data (validateNorwegianIdNumber-exdata "91234567883")))))

  (testing "works with H numbers"
    (is (nil? (ex-data (validateNorwegianIdNumber-exdata "01415612385"))))
    (is (ex-data (validateNorwegianIdNumber-exdata "01535612303"))))

  (testing "belongs to a person born in the 1900s if the three first digits are in the [0, 500) range"
    (is (= [18] (possibleAgesOfPersonWithIdNumber "03119849925" "19022017"))))

  (testing "belongs to a person born in the 1800s or 2000s if the three first digits are in the [500, 750) range"
    (is (= [118] (possibleAgesOfPersonWithIdNumber "04119850938" "19022017")))
    (is (= [14] (possibleAgesOfPersonWithIdNumber "04110250989" "19022017"))))

  (testing "belongs to a person born in the 1900s or 2000s if the three first digits are in the [900, 1000) range"
    (is (= [101 1] (possibleAgesOfPersonWithIdNumber "03111590981" "19022017")))
    (is (= [60] (possibleAgesOfPersonWithIdNumber "03115690905" "19022017"))))

  (testing "belongs to a person born in the 2000s if the three first digits are in the [750, 900) range"
    (is (= [15] (possibleAgesOfPersonWithIdNumber "03110175255" "19022017")))
    (is (= [] (possibleAgesOfPersonWithIdNumber "03119975246" "19022017"))))

  (testing "empty string should be treated as invalid national id number"
    (is (ex-data (validateNorwegianIdNumber-exdata ""))))

  (testing "nil should be treated as invalid national id number"
    (is (= :not-str-input (:code (ex-data (validateNorwegianIdNumber-exdata nil))))))

  (testing "other datatypes than string should be treated as invalid national id number"
    (is (= :not-str-input (:code (ex-data (validateNorwegianIdNumber-exdata ["Hello" "World"]))))))

  (testing "is not part of an FH number"
    (is (empty? (possibleAgesOfPersonWithIdNumber "83119849925" "19022017")))))

#_(defn list-valid-numbers [ddmmyy]
  (doseq [i (range 10000 99999)]
    (when (validate-norwegian-id-number (str ddmmyy i))
      (println (str ddmmyy i)))))