(ns iron-cache.integration-test
  (:use clj-http.fake)
  (:require [clojure.test :refer :all]
            [iron-cache.core :as ic]))


(defn- response [file-name]
  (slurp (str "test/responses/" file-name)))

(defonce client (ic/new-client {:project "amiga" :token "abcd-abcd-abcd"}))

(defonce valid-server-url (str "https://" @#'ic/ROOT_URL "/1"))

(def list-200
  {(str valid-server-url "/amiga/caches") (fn [_] {:status 200 :body (response "list-200")})})

(def list-200-empty
  {(str valid-server-url "/amiga/caches") (fn [_] {:status 200 :body "[]"})})

(def list-401
  {(str valid-server-url "/amiga/caches") (fn [_] {:status 401 :body (response "list-401")})})

(def list-500
  {(str valid-server-url "/amiga/caches") (fn [_] {:status 500 :body (response "list-500")})})


(deftest test-list
  (testing "correct list of caches"
    (with-fake-routes list-200
      (let [resp (ic/list client)]
        (is (= 200 (-> resp :status)))
        (is (= 2 (-> resp :msg count)))
        (is (= "amiga" (-> resp :msg first :project_id)))
        (is (= "b" (-> resp :msg last :name))))))

  (testing "empty list of caches"
    (with-fake-routes list-200-empty
      (let [resp (ic/list client)]
        (is (= 200 (-> resp :status)))
        (is (= 0 (-> resp :msg count)))
        (is (= nil (-> resp :msg first :project_id))))))

  (testing "non-authorized request"
    (with-fake-routes list-401
      (let [resp (ic/list client)]
        (is (= 401 (-> resp :status)))
        (is (= "You must be authorized" (-> resp :msg))))))

  (testing "server went down"
    (with-fake-routes list-500
      (let [resp (ic/list client)]
        (is (= 500 (-> resp :status)))
        (is (= "Iron Server went down" (-> resp :msg)))))))
