(defproject iron-cache "1.0.0"

  :description "Clojure client for IronCache by www.iron.io"

  :url "https://github.com/kolotaev/iron-cache"

  :scm {:name "github"
        :url "https://github.com/kolotaev/iron-cache/tree/master"}

  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}

  :dependencies [[org.clojure/clojure "1.8.0"]
                 [cheshire "5.7.1"]
                 [clj-http "3.7.0"]]

  :plugins [[lein-cloverage "1.0.10-SNAPSHOT"]]

  :profiles {:dev {:dependencies [[ring/ring-jetty-adapter "1.6.1"]
                                  [clj-http-fake "1.0.3"]]}}

  :test-selectors {:default (constantly true)
                   :unit  #(not (:integration %))
                   :integration :integration})
