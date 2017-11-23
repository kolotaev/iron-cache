(defproject iron-cache "0.1.0-SNAPSHOT"

  :description "Clojure client for IronCache by www.iron.io"

  :url "https://github.com/kolotaev/iron-cache"

  :scm {:name "github"
        :url "https://github.com/kolotaev/iron-cache/tree/master"}

  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}

  :dependencies [[org.clojure/clojure "1.8.0"]
                 [clj-http "3.7.0"]]

  :profiles {:dev {:dependencies [[ring/ring-jetty-adapter "1.6.1"]
                                  [clj-http-fake "1.0.3"]]}})
