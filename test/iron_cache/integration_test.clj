(ns iron-cache.integration-test
  (:require [clojure.test :refer :all]
            [iron-cache.sync :refer :all]
            [ring.adapter.jetty :as ring]))

(defn iron-server-mock-handler [req]
  (condp = [(:request-method req) (:uri req)]
    [:get "/foo"]
    {:status 200 :body "foo!"}))

(defn run-server []
  (defonce server
    (ring/run-jetty iron-server-mock-handler {:port 19980 :join? false})))


(deftest ^:integration foo-bar
  (run-server)
  (let [resp (http-client/request {:scheme :http
                              :server-name "localhost"
                              :server-port 19980
                              :request-method :get :uri "/foo"
                              :content-type "text/plain"})]
    (is (= "foo!" (:body resp)))))
