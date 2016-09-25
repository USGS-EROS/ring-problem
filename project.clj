(defproject ring/ring-problem "0.1.0-SNAPSHOT"
  :description "Ring Middleware - Problem Details for HTTP APIs (RFC 7807)"
  :url "http://github.com/jmorton/ring-problem"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [cheshire/cheshire "5.6.3"]]
  :profiles {:dev {:resource-paths ["dev"]
                   :dependencies [[org.clojure/tools.namespace "0.3.0-alpha3"]]
                   :plugins [[lein-codox "0.10.0"]
                             [lein-ancient "0.6.10"]
                             [lein-kibit "0.1.2"]
                             [jonase/eastwood "0.2.3"]]}})
