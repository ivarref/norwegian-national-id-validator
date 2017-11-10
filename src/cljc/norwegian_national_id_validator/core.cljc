(ns norwegian-national-id-validator.core
  (:require [clojure.string :as str]))

; Ported from https://github.com/mikaello/norwegian-national-id-validator/blob/master/src/index.js

(def numbers ["0" "1" "2" "3" "4" "5" "6" "7" "8" "9"])
(def str-to-digit (into {} (map-indexed (fn [idx x] [x idx]) numbers)))

(defn parse-pos-int [x]
  (reduce (fn [o v] (+ (* o 10) (get str-to-digit (str v)))) 0 (str x)))

(defn is-pos-int [x]
  (and (not-empty x)
       (every? #(some #{(str %)} numbers) x)))

(defn is-not-pos-int [x] (not (is-pos-int x)))

(defn leap
  [year]
  (cond (zero? (mod year 400)) true
        (zero? (mod year 100)) false
        (zero? (mod year 4)) true
        :default false))

(def days-in-months [31 28 31 30 31 30 31 31 30 31 30 31])
(def days-in-months-leap [31 29 31 30 31 30 31 31 30 31 30 31])

(defn is-valid-date-months [DD MM YYYY dim]
  (and (>= DD 1)
       (>= MM 1)
       (<= MM 12)
       (<= DD (nth dim (dec MM)))))

(defn is-valid-date [dateDDMMYYYY]
  {:pre [(= 8 (count dateDDMMYYYY))]}
  (let [DD (parse-pos-int (subs dateDDMMYYYY 0 2))
        MM (parse-pos-int (subs dateDDMMYYYY 2 4))
        YYYY (parse-pos-int (subs dateDDMMYYYY 4 8))]
    (is-valid-date-months DD MM YYYY (if (leap YYYY) days-in-months-leap days-in-months))))

(defn now-DDMMYYYY []
  {:post [(is-valid-date %)]}
  #?(:clj  (.format (java.time.LocalDate/now) (java.time.format.DateTimeFormatter/ofPattern "ddMMyyyy"))
     :cljs (let [now (js/Date.)
                 dd (.getDate now)
                 mm (inc (.getMonth now))
                 yyyy (.getFullYear now)]
             (str (when (< dd 10) "0") dd
                  (when (< mm 10) "0") mm
                  yyyy))))

(defn diff-years [a b]
  {:pre [(= 8 (count a)) (= 8 (count b))]}
  (let [aDD (parse-pos-int (subs a 0 2))
        aMM (parse-pos-int (subs a 2 4))
        aYYYY (parse-pos-int (subs a 4 8))
        bDD (parse-pos-int (subs b 0 2))
        bMM (parse-pos-int (subs b 2 4))
        bYYYY (parse-pos-int (subs b 4 8))
        diff (- bYYYY aYYYY)]
    (if (or (> aMM bMM) (and (= aMM bMM) (> aDD bDD)))
      (dec diff)
      diff)))

(defn isValidCheckDigit [staticSequence elevenDigits]
  {:pre [(every? number? elevenDigits)]}
  (let [productSum (reduce (fn [acc [idx x]] (+ acc (* x (nth elevenDigits idx))))
                           0
                           (map-indexed (fn [idx x] [idx x]) staticSequence))]
    (= (mod productSum 11) 0)))

(defn isValidCheckDigits [elevenDigits]
  (let [staticSequenceFirstCheckDigit [3 7 6 1 8 9 4 5 2 1]
        staticSequenceSecondCheckDigit [5 4 3 2 7 6 5 4 3 2 1]
        elevenDigitsArray (mapv parse-pos-int elevenDigits)]
    (and (isValidCheckDigit staticSequenceFirstCheckDigit elevenDigitsArray)
         (isValidCheckDigit staticSequenceSecondCheckDigit elevenDigitsArray))))

(defn idNumberType [elevenDigits]
  (let [firstDigit (parse-pos-int (first elevenDigits))
        thirdDigit (parse-pos-int (nth elevenDigits 2))]
    (cond (or (= firstDigit 8) (= firstDigit 9)) :FHNumber
          (and (>= firstDigit 4) (<= firstDigit 7)) :DNumber
          (or (= thirdDigit 4) (= thirdDigit 5)) :HNumber
          :else :birthNumber)))

(defn century-prefixes [ageGroupNumber]
  (let [prefixes ["19"]
        prefixes (if (and (>= ageGroupNumber 500) (< ageGroupNumber 1000)) ["20"] prefixes)
        prefixes (if (and (>= ageGroupNumber 500) (< ageGroupNumber 750)) ["20" "18"] prefixes)
        prefixes (if (and (>= ageGroupNumber 900) (< ageGroupNumber 1000)) ["19" "20"] prefixes)]
    prefixes))

(defn date [elevenDigitsWithDDMMYY]
  {:pre [(= 11 (count elevenDigitsWithDDMMYY))]}
  (let [DDMM (subs elevenDigitsWithDDMMYY 0 4)
        YY (subs elevenDigitsWithDDMMYY 4 6)
        ageGroupNumber (parse-pos-int (subs elevenDigitsWithDDMMYY 6 9))
        centuryPrefixes (century-prefixes ageGroupNumber)]
    (->> centuryPrefixes
         (mapv #(str DDMM % YY))
         (filter is-valid-date))))

(defn possibleBirthDateOfBirthNumber [elevenDigits] (date elevenDigits))

(defn possibleBirthDateOfDNumber [elevenDigits]
  (date (str (- (parse-pos-int (nth elevenDigits 0)) 4)
             (subs elevenDigits 1))))

(defn possibleBirthDateOfHNumber [elevenDigits]
  (date (str (subs elevenDigits 0 2)
             (- (parse-pos-int (nth elevenDigits 2)) 4)
             (subs elevenDigits 3 11))))

(defn possibleBirthDatesOfIdNumber [elevenDigits]
  (let [typ (idNumberType elevenDigits)]
    (cond (= :birthNumber typ) (possibleBirthDateOfBirthNumber elevenDigits)
          (= :DNumber typ) (possibleBirthDateOfDNumber elevenDigits)
          (= :HNumber typ) (possibleBirthDateOfHNumber elevenDigits)
          :else [])))

(defn possibleAgesOfPersonWithIdNumber [elevenDigits nowddMMyyyy]
  {:pre [(= 11 (count elevenDigits))
         (= 8 (count nowddMMyyyy))]}
  (->> (possibleBirthDatesOfIdNumber elevenDigits)
       (mapv #(diff-years % nowddMMyyyy))
       (filter #(and (>= % 0) (< % 125)))
       (vec)))

(defn validateNorwegianIdNumber-exdata
  ([idNumber] (validateNorwegianIdNumber-exdata idNumber (now-DDMMYYYY)))
  ([idNumber nowddMMyyyy]
   (let [trimmed (str/trim (if (not (string? idNumber)) "" idNumber))]
     (cond (not (string? idNumber)) (ex-info "Not string input" {:value idNumber :code :not-str-input})
           (is-not-pos-int trimmed) (ex-info "Not a number" {:value trimmed :code :nan})
           (not= 11 (count trimmed)) (ex-info "Incorrect length" {:value trimmed :code :count})
           (not (isValidCheckDigits trimmed)) (ex-info "Digit modulo incorrect" {:value trimmed :code :digitcheck})
           (= :FHNumber (idNumberType trimmed)) true
           (empty? (possibleAgesOfPersonWithIdNumber trimmed nowddMMyyyy)) (ex-info "No possible ages of person" {:value trimmed :code :age})
           :else true))))

(defn validate-norwegian-id-number [idNumber]
  (true? (validateNorwegianIdNumber-exdata idNumber)))
