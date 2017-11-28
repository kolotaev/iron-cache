(ns iron-cache.async-test
  (:require [clojure.test :refer :all]
            [iron-cache.core :as ic]
            [ring.adapter.jetty :as ring]
            [cheshire.core :as json]))


(def wait-ms 5000)

(def URL "http://localhost:19980")

(defonce client
  (ic/new-client {:host "http://localhost:19980" :project "amiga" :token "abcd-asdf-qwer"}))

(defonce client-no-body-parse
  (ic/new-client {:host "http://localhost:19980" :project "amiga" :token "a" :callbacks-parse false}))

(defn- response [file-name]
  (slurp (str "test/responses/" file-name)))

(defn iron-server-mock-handler [req]
  (condp = [(:request-method req) (:uri req)]
    [:get "/1/amiga/caches"] {:status 200 :body (response "list-200")}))

(defn run-server []
  (defonce server
    (ring/run-jetty iron-server-mock-handler {:port 19980 :join? false})))


;;; Tests ;;;

(deftest ^:integration ^:async cache-list-async
  (run-server)
  (testing "correct list of caches with baked-in response body parser"
    (let [result (promise)
          _ (ic/list client {:ok #(deliver result %)
                             :fail #(deliver result %)})
          resp (deref result wait-ms :timeout)]
      (is (some? resp))
      (is (= 200 (:status resp)))
      (is (= 2 (-> resp :msg count)))
      (is (= "amiga" (-> resp :msg first :project_id)))
      (is (= "b" (-> resp :msg last :name)))))

  (testing "correct list of caches with custom callbacks"
    (let [result (promise)
          _ (ic/list client-no-body-parse {:ok #(deliver result %)
                             :fail #(deliver result %)})
          resp (deref result wait-ms :timeout)]
      (is (some? resp))
      (is (= 2 (-> resp :body count)))
      (is (= "a" (-> resp :body first :name))))))
