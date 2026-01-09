(ns norwegian-national-id-validator.core
  "Validering av fødselsnummer og/eller dnummer. Støtter nytt format som trer i kraft 2032.
  Direkte omskriving av kildekode fra
  https://skatteetaten.github.io/folkeregisteret-api-dokumentasjon/nytt-fodselsnummer-fra-2032/")

(def vekter-k1 [3 7 6 1 8 9 4 5 2])
(def vekter-k2 [5 4 3 2 7 6 5 4 3 2])
(def gyldig-rest-k1 #{0 1 2 3})
(def gyldig-rest-k2 0)

(def char->int
  #?(:cljs int
     :clj (fn [i] (Character/digit i 10))))

(defn valider-kontrollsiffer-foedsels-eller-Dnummer
  "Validerer et fødsels-eller-d-nummer(1964 og 2032-type) ved å sjekke kontrollsifrene iht.
  https://skatteetaten.github.io/folkeregisteret-api-dokumentasjon/nytt-fodselsnummer-fra-2032/.
  @param {string} fnr-dnr - 11-siffret fødsels-eller-D-nummer som skal valideres.
  @returns {boolean} - Returnerer true hvis fødselsnummeret er gyldig, ellers false."
  [fnr-dnr]
  (let [sifre (map char->int fnr-dnr)
        gitt-k1 (nth sifre 9)
        gitt-k2 (nth sifre 10)

        grunnlag-k1 (take (count vekter-k1) sifre)
        vektet-k1 (reduce + (map * grunnlag-k1 vekter-k1))
        beregnet-rest-siffer-k1 (mod (+ vektet-k1 gitt-k1) 11)]

    (if (not (contains? gyldig-rest-k1 beregnet-rest-siffer-k1))
      false
      (let [grunnlag-k2 (take (count vekter-k2) sifre)
            vektet-k2 (reduce + (map * grunnlag-k2 vekter-k2))
            beregnet-rest-siffer-k2 (mod (+ vektet-k2 gitt-k2) 11)]
        (= beregnet-rest-siffer-k2 gyldig-rest-k2)))))

(def dNummer-sifre #{\4 \5 \6 \7})

(defn er-Dnummer?
  "Sjekker om et gitt nummer er et D-nummer.
  @param {string} gitt-nummer - Nummeret som skal sjekkes.
  @returns {boolean} - Returnerer true hvis nummeret er et D-nummer, ellers false."
  [gitt-nummer]
  (contains? dNummer-sifre (first gitt-nummer)))

(defn- fnr->dato
  [gitt-nummer er-syntetisk?]
  (let [dato (subs gitt-nummer 0 6)]
    (cond er-syntetisk?
          (let [måned-siffer (char->int (nth dato 2))
                _ (when (< måned-siffer 8)
                    (throw (ex-info "Ugyldig format: datoen i syntetiske nummer må være i formatet dd8Myy eller dd9Myy" {:gitt-nummer gitt-nummer})))
                normalisert-mnd (- måned-siffer 8)]
            (str (subs dato 0 2)
                 normalisert-mnd
                 (subs dato 3 6)))

          (er-Dnummer? gitt-nummer)
          (let [dag-siffer (char->int (nth dato 0))]
            (str (- dag-siffer 4) (subs dato 1 6)))

          :else dato)))

(defn- skuddår?
  "Utleder om et gitt år er et skuddår basert på kun to sifre. Dette medfører at man ikke
  kan vite hvilket århundre det gjelder, så velger å anse '00' som skuddåret 2000.
  Dette er grunnet i det ikke lengre vil være mulig å utlede århundre av 2032-fødselsnumre.
  @param {string} år - Året som skal sjekkes i formatet 'yy'.
  @returns {boolean} - Returnerer true hvis året er et skuddår, ellers false."
  [år]
  (zero? (mod (parse-long år) 4)))

(defn- skuddag?
  [dag måned]
  (= (str dag måned) "2902"))

(defn er-dato-gyldig?
  "Sjekker om en gitt dato finnes på en kalender. Da århundre ikke lengre vil kunne utledes av
  2032-fødselsnumre, antas alle datoer å være etter år 2000.
  @param {string} dato - Datoen som skal sjekkes i formatet ddMMyy.
  @returns {boolean} - Returnerer true hvis datoen er gyldig, ellers false."
  [dato]
  (let [århundre "20"
        år   (subs dato 4 6)
        måned (subs dato 2 4)
        dag   (subs dato 0 2)]
    (if (and (skuddag? dag måned)
             (not (skuddår? år)))
      false
      #?(:clj (try (let [formatter (java.time.format.DateTimeFormatter/ofPattern "ddMMyyyy")]
                     (java.time.LocalDate/parse (str dag måned århundre år) formatter)
                     true)
                   (catch java.time.format.DateTimeParseException _ false))
         :cljs (not (js/isNaN (.getDate (js/Date. (str århundre år "/" måned "/" dag)))))))))


(defn valider-id
  "Validerer at gitt ID har gyldig format og dato.
  @param {string} gitt-nummer - ID-nummer som skal valideres.
  @param {boolean} syntetisk? - Angir om ID-nummeret er syntetisk.
  @throws {Error} - Kaster en feil med en spesifikk melding hvis valideringen feiler.
  @returns {boolean} - Returnerer true hvis ingen feil."
  [gitt-nummer syntetisk?]
  (when-not (string? gitt-nummer)
    (throw (ex-data (ex-info "Ugyldig input: ID må være en streng" {:gitt-nummer gitt-nummer}))))
  (or (re-matches #"\d{11}" gitt-nummer)
      (throw (ex-data (ex-info "Ugyldig format: ID må være 11 sifre" {}))))
  (let [dato (fnr->dato gitt-nummer syntetisk?)]
    (when-not (er-dato-gyldig? dato)
      (throw (ex-info "Ugyldig format: id har ugyldig dato i formatet ddMMyy"
                      {:gitt-nummer gitt-nummer
                       :dato        dato})))

    (when-not (valider-kontrollsiffer-foedsels-eller-Dnummer gitt-nummer)
      (throw (ex-info "Ugyldig ID: er ikke gyldig bygget opp som gyldig nummer"
                      {:gitt-nummer gitt-nummer
                       :syntetisk?  syntetisk?})))

    true))

(defn norwegian-id-number?
  "Returns true iff `idnumber` is a valid Norwegian ID number."
  [id-number & [syntetisk?]]
  (try (or (true? (valider-id id-number syntetisk?))
           (and (nil? syntetisk?)
                (valider-id id-number true)))
       (catch #?(:clj  Exception
                 :cljs js/Error) _
         (and (nil? syntetisk?)
              (norwegian-id-number? id-number true)))))
