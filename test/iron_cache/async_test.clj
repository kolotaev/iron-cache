(ns iron-cache.async-test
  (:require [clojure.test :refer :all]
            [iron-cache.core :as ic]
            [iron-cache.response-utils :as r]
            [ring.adapter.jetty :as ring]
            [cheshire.core :as json]))


(def wait-ms 5000)

(def port 19980)

(def URL (str "http://localhost:" port))


(defonce client
  (ic/new-client {:host URL :project :amiga :token "a"}))

(defonce client-no-body-parse
  (ic/new-client {:host URL :project "amiga" :token "b" :parse-callbacks false}))

(defonce client-who-fails
  (ic/new-client {:host "http://localhost:11111111"
                  :project "amiga"
                  :token "b"
                  :http-options {:throw-exceptions true}
                  :parse-callbacks false}))


(defn iron-server-mock-handler [req]
  (condp = [(:request-method req) (:uri req)]
    [:get "/1/amiga/caches"]
    {:status 200 :body (r/response "list-200")}

    [:delete "/1/amiga/caches/sports/items/football"]
    {:status 200 :body (r/response "delete-key")}

    [:post "/1/amiga/caches/drinks/items/vodka-amount/increment"]
    {:status 200
     :body (->>
             req
             :body
             r/is->map
             :amount
             (+ 100)
             (assoc {"msg" "Added"} "value")
             json/generate-string)}

    [:put "/1/amiga/caches/credit-cards/items/basic"]
    {:status 200
     :body (let [body (-> req :body r/is->map)]
             (if (and
                   (map? body)
                   (some? (:value body)))
               (r/response "put-key-201")
               (r/response "general-400")))}

    [:put "/1/amiga/caches/credit-cards/items/additional"]
    {:status 200
     :body (let [body (-> req :body r/is->map)]
            (if (and
                  (map? body)
                  (some? (:value body))
                  (some? (:expires_in body))
                  (some? (:replace body)))
              (r/response "put-key-201")
              (r/response "general-400")))}))

(defn run-server []
  (defonce server
    (ring/run-jetty iron-server-mock-handler {:port port :join? false})))


;;; Tests ;;;

(deftest ^:integration ^:async cache-list-async
  (run-server)
  (testing "correct list of caches with baked-in response body parser"
    (let [result (promise)
          _ (ic/list client {:ok #(deliver result %)})
          resp (deref result wait-ms :timeout)]
      (is (= 2 (count resp)))
      (is (= "amiga" (-> resp first :project_id)))
      (is (= "b" (-> resp last :name)))))

  (testing "correct list of caches with custom callbacks"
    (let [result (promise)
          _ (ic/list client-no-body-parse {:ok #(deliver result %)
                                           :fail #(deliver result %)})
          resp (deref result wait-ms :timeout)]
      (is (= 2 (-> resp :body count)))
      (is (= "a" (-> resp :body first :name)))))

  (testing "response failure handling works"
    (let [result (promise)
          _ (ic/list client-who-fails {:fail #(deliver result %)})
          resp (deref result wait-ms :timeout)
          exception-msg (.getMessage resp)]
      (is (string? exception-msg))
      (is (re-find #"port out of range" exception-msg)))))


(deftest ^:integration ^:async key-del-async
  (run-server)
  (testing "correct deletion of key with baked-in response body parser"
    (let [result (promise)
          _ (ic/del client :sports :football {:ok #(deliver result %)})
          resp (deref result wait-ms :timeout)]
      (is (= "Deleted" (:msg resp)))))

  (testing "correct deletion of key with custom callbacks"
    (let [result (promise)
          _ (ic/del client-no-body-parse :sports :football {:ok #(deliver result %)
                                           :fail #(deliver result %)})
          resp (deref result wait-ms :timeout)]
      (is (= 200 (:status resp)))
      (is (= "Deleted" (-> resp :body :msg)))))

  (testing "response failure handling works"
    (let [result (promise)
          _ (ic/del client-who-fails :sports :football {:fail #(deliver result %)})
          resp (deref result wait-ms :timeout)
          exception-msg (.getMessage resp)]
      (is (string? exception-msg))
      (is (re-find #"port out of range" exception-msg)))))


(deftest ^:integration ^:async key-incr-async
  (run-server)
  (testing "correct item increment given positive amount with baked-in response body parser"
    (let [result (promise)
          _ (ic/incr client :drinks "vodka-amount" 10 {:ok #(deliver result %)})
          resp (deref result wait-ms :timeout)]
      (is (map? resp))
      (is (= "Added" (:msg resp)))
      (is (= 110 (:value resp)))))

  (testing "correct item increment given negative amount with baked-in response body parser"
    (let [result (promise)
          _ (ic/incr client :drinks "vodka-amount" -10 {:ok #(deliver result %)})
          resp (deref result wait-ms :timeout)]
      (is (map? resp))
      (is (= "Added" (:msg resp)))
      (is (= 90 (:value resp)))))

  (testing "correct item increment given float amount with baked-in response body parser"
    (let [result (promise)
          _ (ic/incr client :drinks "vodka-amount" 10.5 {:ok #(deliver result %)})
          resp (deref result wait-ms :timeout)]
      (is (map? resp))
      (is (= "Added" (:msg resp)))
      (is (= 110.5 (:value resp)))))

  (testing "correct item increment with custom callbacks"
    (let [result (promise)
          _ (ic/incr client-no-body-parse :drinks :vodka-amount 34 {:ok #(deliver result %)
                                                                    :fail #(deliver result %)})
          resp (deref result wait-ms :timeout)]
      (is (map? resp))
      (is (= "Added" (-> resp :body :msg)))
      (is (= 134 (-> resp :body :value)))))

  (testing "response failure handling works"
    (let [result (promise)
          _ (ic/incr client-who-fails :drinks :vodka-amount 34 {:fail #(deliver result %)})
          resp (deref result wait-ms :timeout)
          exception-msg (.getMessage resp)]
      (is (string? exception-msg))
      (is (re-find #"port out of range" exception-msg)))))


(deftest ^:integration ^:async key-put-async
  (run-server)
  (testing "correct item put using basic data with baked-in response body parser"
    (let [result (promise)
          _ (ic/put client :credit-cards :basic {:value 85} {:ok #(deliver result %)})
          resp (deref result wait-ms :timeout)]
      (is (map? resp))
      (is (= "Stored." (:msg resp)))))

  (testing "correct item put using compound data with baked-in response body parser"
    (let [result (promise)
          _ (ic/put client :credit-cards :additional
              {:value 85, "expires_in" 456, :replace true}
              {:ok #(deliver result %)})
          resp (deref result wait-ms :timeout)]
      (is (map? resp))
      (is (= "Stored." (:msg resp)))))

  (testing "response failure handling works"
    (let [result (promise)
          _ (ic/put client-who-fails :credit-cards :basic {:value 90} {:fail #(deliver result %)})
          resp (deref result wait-ms :timeout)
          exception-msg (.getMessage resp)]
      (is (string? exception-msg))
      (is (re-find #"port out of range" exception-msg)))))
