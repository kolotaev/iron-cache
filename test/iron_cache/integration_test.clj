(ns iron-cache.integration-test
  (:require [clojure.test :refer :all]
            [iron-cache.sync :refer :all]
            [ring.adapter.jetty :as ring]))

(defn handler [req]
  (condp = [(:request-method req) (:uri req)]
    [:get "/foo"]
    {:status 200 :body "foo!"}))

(defn run-server
  []
  (defonce server
    (ring/run-jetty handler {:port 19980 :join? false})))

