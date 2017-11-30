(ns iron-cache.client-test
  (:use [clj-http.fake]
        [iron-cache.response-utils])
  (:require [clojure.test :refer :all]
            [iron-cache.core :as ic]))


(defonce client (ic/new-client {:project "amiga" :token "abcd-asdf-qwer"}))


(deftest request-sanity
  (testing "Response has only data response in case of successful response"
    (with-fake-routes list-200
      (let [resp (ic/list client)]
        (is (nil? (:status resp)))
        (is (seq? resp)))))

  (testing "Response has only :status and :msg fields in case of invalid response"
    (with-fake-routes list-401
      (let [resp (ic/list client)]
        (is (= [:msg :status] (-> resp keys))))))

  (testing "oauth2 token was sent correctly"
    (with-fake-routes echo-list
      (let [headers (-> client ic/list :original-request :headers)]
        (is (contains? headers :OAuth))
        (is (= "abcd-asdf-qwer" (:OAuth headers))))))

  (testing "content-type header is json"
    (with-fake-routes echo-list
      (let [headers (-> client ic/list :original-request :headers)]
        (is (contains? headers :content-type))
        (is (= "application/json" (:content-type headers))))))

  (testing "accept header is json"
    (with-fake-routes echo-list
      (let [headers (-> client ic/list :original-request :headers)]
        (is (contains? headers :accept))
        (is (= "application/json" (:accept headers))))))

  (testing "cache id (string, keyword, symbol) is correctly formatted in a request URI"
    (with-fake-routes echo-info
      (are [id] (= "/1/amiga/caches/my-cache-id" (-> client (ic/info id) :original-request :uri))
        "my-cache-id"
        :my-cache-id
        'my-cache-id)))

  (testing "project id (string, keyword, symbol) is correctly formatted in a request URI"
    (with-fake-routes echo-list
      (are [project-id](= "/1/amiga/caches" (-> {:project project-id :token "abc"}
                                                ic/new-client
                                                ic/list
                                                :original-request :uri))
        "amiga"
        :amiga
        'amiga))))


(deftest cache-list
  (testing "correct list of caches"
    (with-fake-routes list-200
      (let [resp (ic/list client)]
        (is (seq? resp))
        (is (= 2 (count resp)))
        (is (= "amiga" (-> resp first :project_id)))
        (is (= "b" (-> resp last :name))))))

  (testing "empty list of caches"
    (with-fake-routes list-200-empty
      (let [resp (ic/list client)]
        (is (= 0 (count resp)))
        (is (= nil (-> resp first :project_id))))))

  (testing "non-authorized request"
    (with-fake-routes list-401
      (let [resp (ic/list client)]
        (is (= 401 (:status resp)))
        (is (= "You must be authorized" (:msg resp))))))

  (testing "server went down"
    (with-fake-routes list-500
      (let [resp (ic/list client)]
        (is (= 500 (:status resp)))
        (is (= "Iron Server went down" (:msg resp)))))))


(deftest cache-info
  (testing "correct info of about a cache"
    (with-fake-routes info-200
      (let [resp (ic/info client "credit-cards")]
        (is (map? resp))
        (is (= 80000 (:size resp))))))

  (testing "empty info about a cache"
    (with-fake-routes info-200-empty
      (let [resp (ic/info client "users")]
        (is (= nil (:size resp))))))

  (testing "forbidden request"
    (with-fake-routes info-403
      (let [resp (ic/info client :planes.my)]
        (is (= 403 (:status resp)))
        (is (= "Project suspected, resource limits" (:msg resp))))))

  (testing "server went down"
    (with-fake-routes info-500
      (let [resp (ic/info client "users")]
        (is (= 500 (:status resp)))
        (is (= "Iron Server went down" (:msg resp)))))))


(deftest cache-delete!
  (testing "correct deletion of a cache"
    (with-fake-routes delete-cache
      (let [resp (ic/delete! client "credit-cards")]
        (is (map? resp))
        (is (= "Deleted" (:msg resp))))))

  (testing "server went down"
    (with-fake-routes delete-cache-500
      (let [resp (ic/delete! client "users")]
        (is (= 500 (:status resp)))
        (is (= "Iron Server went down" (:msg resp)))))))


(deftest cache-clear!
  (testing "correct clear of a cache"
    (with-fake-routes clear-cache
      (let [resp (ic/clear! client "credit-cards")]
        (is (map? resp))
        (is (= "Cleared." (:msg resp))))))

  (testing "server went down"
    (with-fake-routes clear-cache-500
      (let [resp (ic/clear! client :users)]
        (is (= 500 (:status resp)))
        (is (= "Iron Server went down" (:msg resp)))))))


(deftest key-get
  (testing "correct key retrieve"
    (with-fake-routes get-key-200
      (let [resp (ic/get client "users" :john)]
        (is (map? resp))
        (is (= "john" (-> resp :key)))
        (is (= 25 (-> resp :value :age)))
        (is (= 12345 (-> resp :cas))))))

  (testing "key not found"
    (with-fake-routes get-key-404
      (let [resp (ic/get client "users" "alice")]
        (is (= 404 (:status resp)))
        (is (= "Key was not found" (:msg resp))))))

  (testing "server went down"
    (with-fake-routes get-key-500
      (let [resp (ic/get client :users "john")]
        (is (= 500 (:status resp)))
        (is (= "Iron Server went down" (:msg resp)))))))


(deftest key-incr
  (testing "key was incremented successfully given positive amount"
    (with-fake-routes incr-key-201
      (let [resp (ic/incr client :credit-cards :1234 10)]
        (is (map? resp))
        (is (= "Added" (:msg resp)))
        (is (= 100 (:value resp))))))

  (testing "server went down"
    (with-fake-routes incr-key-500
      (let [resp (ic/incr client :users "john" 140)]
        (is (= 500 (:status resp)))
        (is (= "Iron Server went down" (:msg resp)))))))


(deftest key-put
  (testing "simlpe value was put sucessfully"
    (with-fake-routes put-key-201-minimal
      (let [resp (ic/put client :credit-cards 1234 {:value 85})]
        (is (map? resp))
        (is (= "Stored." (:msg resp))))))

  (testing "compound value was put sucessfully"
    (with-fake-routes put-key-201-additional
      (let [resp (ic/put client :credit-cards :1234 {:value 85, "expires_in" 456, :replace true})]
        (is (map? resp))
        (is (= "Stored." (:msg resp))))))

  (testing "server went down"
    (with-fake-routes put-key-500
      (let [resp (ic/put client :users "john" {:value "aaa"})]
        (is (= 500 (:status resp)))
        (is (= "Iron Server went down" (:msg resp)))))))


(deftest key-del
  (testing "key was deleted successfully"
    (with-fake-routes delete-key
      (let [resp (ic/del client :credit-cards 1234)]
        (is (map? resp))
        (is (= "Deleted" (:msg resp))))))

  (testing "server went down"
    (with-fake-routes delete-key-500
      (let [resp (ic/del client :users "john")]
        (is (= 500 (:status resp)))
        (is (= "Iron Server went down" (:msg resp)))))))
