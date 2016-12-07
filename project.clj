(defproject inc-align "0.1.0-SNAPSHOT"
  :description "Berkeley Aligner extension for incremental alignment"
  :url "http://example.com/FIXME"
  :license {:name "GNU General Public License"
            :url "http://www.gnu.org/licenses/old-licenses/gpl-2.0.html"}
  :dependencies [[org.clojure/clojure "1.8.0"]
                [berkeley/aligner "1.0.0"]
                [org.clojure/tools.cli "0.3.5"]]
  :repositories {"local" ~(str (.toURI (java.io.File. "lib")))}
  :main inc-align.core
  :aot [inc-align.core inc-align.combine]
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}})
