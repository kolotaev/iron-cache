(defproject iron-cache "0.1.0-SNAPSHOT"

  :description "Clojure client for IronCache by www.iron.io"

  :url "https://github.com/kolotaev/iron-cache"

  :scm {:name "github"
        :url "https://github.com/kolotaev/iron-cache/tree/master"}

  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}

  :dependencies [[org.clojure/clojure "1.8.0"]
                 [clj-http "3.7.0"]
                 [http.async.client "1.2.0"]]

  :profiles {:dev {:dependencies [
;                                  [org.clojure/clojure "1.8.0"]
;                                  [ring/ring-codec "1.0.1"]
;[org.clojure/tools.logging "0.3.1"]
[log4j "1.2.17"]
                                  [ring/ring-jetty-adapter "1.6.1"]
;                                  [ring/ring-devel "1.6.1"]
                                  ]}})
