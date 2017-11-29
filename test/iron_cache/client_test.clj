(ns iron-cache.client-test
  (:use clj-http.fake)
  (:require [clojure.test :refer :all]
            [iron-cache.core :as ic]
            [cheshire.core :as json]))


(defn- response [file-name]
  (slurp (str "test/responses/" file-name)))

(defonce client (ic/new-client {:project "amiga" :token "abcd-asdf-qwer"}))

(defonce valid-server-url (str @#'ic/ROOT_URL "/1"))


;; Mock Routes ;;

(def echo-list
  {(str valid-server-url "/amiga/caches")
   (fn [req] {:body (-> {:original-request req} json/generate-string)})})

(def echo-info
  {(str valid-server-url "/amiga/caches/my-cache-id")
   (fn [req] {:body (-> {:original-request req} json/generate-string)})})

;;; list ;;;
(def list-200
  {(str valid-server-url "/amiga/caches") (fn [_] {:status 200 :body (response "list-200")})})

(def list-200-empty
  {(str valid-server-url "/amiga/caches") (fn [_] {:status 200 :body "{}"})})

(def list-401
  {(str valid-server-url "/amiga/caches") (fn [_] {:status 401 :body (response "list-401")})})

(def list-500
  {(str valid-server-url "/amiga/caches") (fn [_] {:status 500 :body (response "list-500")})})

;;;; info ;;;
(def info-200
  {(str valid-server-url "/amiga/caches/credit-cards") (fn [_] {:status 200 :body (response "info-200")})})

(def info-200-empty
  {(str valid-server-url "/amiga/caches/users") (fn [_] {:status 200 :body "[]"})})

(def info-403
  {(str valid-server-url "/amiga/caches/planes.my") (fn [_] {:status 403 :body (response "info-403")})})

(def info-500
  {(str valid-server-url "/amiga/caches/users") (fn [_] {:status 500 :body (response "info-500")})})

;;;; delete! ;;;
(def delete-cache
  {(str valid-server-url "/amiga/caches/credit-cards") (fn [_] {:status 200 :body (response "delete-cache")})})

(def delete-cache-500
  {(str valid-server-url "/amiga/caches/users") (fn [_] {:status 500 :body (response "delete-cache-500")})})

;;;; clear! ;;;
(def clear-cache
  {(str valid-server-url "/amiga/caches/credit-cards") (fn [_] {:status 200 :body (response "clear-cache")})})

(def clear-cache-500
  {(str valid-server-url "/amiga/caches/users") (fn [_] {:status 500 :body (response "clear-cache-500")})})


;; Tests ;;

(deftest request-sanity
  (testing "Response has only data response in case of successful response"
    (with-fake-routes list-200
      (let [resp (ic/list client)]
        (is (nil? (:status resp)))
        (is (seq? resp)))))

  (testing "Response has only :status and :msg fields in case of invalid response"
    (with-fake-routes list-401
      (let [resp (ic/list client)]
        (is (= [:status :msg] (-> resp keys))))))

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

  (testing "cache id (string, keyword, symbol) is correctly farmatted in a request URI"
    (with-fake-routes echo-info
      (are [id] (= "/1/amiga/caches/my-cache-id" (-> client (ic/info id) :original-request :uri))
        "my-cache-id"
        :my-cache-id
        'my-cache-id)))

  (testing "project id (string, keyword, symbol) is correctly farmatted in a request URI"
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
      (let [resp (ic/info client "planes.my")]
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
      (let [resp (ic/info client "credit-cards")]
        (is (map? resp))
        (is (= "Deleted" (:msg resp))))))

  (testing "server went down"
    (with-fake-routes delete-cache-500
      (let [resp (ic/info client "users")]
        (is (= 500 (:status resp)))
        (is (= "Iron Server went down" (:msg resp)))))))


(deftest cache-clear!
  (testing "correct clear of a cache"
    (with-fake-routes clear-cache
      (let [resp (ic/info client "credit-cards")]
        (is (map? resp))
        (is (= "Cleared." (:msg resp))))))

  (testing "server went down"
    (with-fake-routes clear-cache-500
      (let [resp (ic/info client "users")]
        (is (= 500 (:status resp)))
        (is (= "Iron Server went down" (:msg resp)))))))
