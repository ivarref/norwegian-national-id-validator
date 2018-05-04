(defproject norwegian-national-id-validator "0.1.3-SNAPSHOT"
  :description "Validate Norwegian national identity numbers (birth number (fødselsnummer), D-number, H-number and FH-number)."
  :url "https://github.com/ivarref/norwegian-national-id-validator"
  :license {:name "Eclipse Public License"
            :url  "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [org.clojure/clojurescript "1.9.946"]]
  :clojurescript? true
  :jar-exclusions [#"\.swp|\.swo|\.DS_Store"]

  :auto-clean false

  :aliases {"test-cljs" ["doo" "rhino" "test" "once"]
            "test"      ["do" "test," "test-cljs"]
            "cleantest" ["do" "clean," "test"]
            "install"   ["do" "clean," "install"]
            "deploy"    ["do" "clean," "deploy" "clojars"]}

  :source-paths ["src/cljc"]
  :cljsbuild {:builds {:main {:source-paths ["src/cljc"]
                              :jar          true
                              :compiler     {:output-to     "demo/js/norwegian-national-id-validator.js"
                                             :optimizations :advanced
                                             :pretty-print  false}}
                       :dev  {:compiler {:optimizations :whitespace
                                         :pretty-print  true}}
                       :test {:source-paths ["src/cljc" "test"]
                              :compiler     {:output-to     "target/unit-test.js"
                                             :output-dir    "target"
                                             :main          norwegian-national-id-validator.cljs-runner
                                             :optimizations :whitespace}}}}
  :profiles {:dev {:jvm-opts     ["-XX:-TieredCompilation"]
                   :dependencies [[criterium "0.4.4" :scope "test"]
                                  [commons-lang "2.6" :scope "test"]
                                  [org.clojure/clojurescript "1.9.946"]
                                  [org.mozilla/rhino "1.7.7"]]
                   :plugins      [[lein-cljsbuild "1.1.7"]
                                  [lein-doo "0.1.8"]]}}
  :doo {:paths {:rhino "lein run -m org.mozilla.javascript.tools.shell.Main"}})
