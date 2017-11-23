(ns iron-cache.integration-test
  (:use clj-http.fake)
  (:require [clojure.test :refer :all]
            [iron-cache.core :as ic]))


(defonce client (ic/new-client {:project "a" :token "b"}))

(defonce valid-server-url (str "https://" @#'ic/ROOT_URL "/1"))

(def list-200
  {"http://cache-aws-us-east-1.iron.io/1" (fn [_] {:status 200 :headers {} :body "kk"})})


(deftest list
  (testing "basic cache list"
    (with-fake-routes list-200
      (is (= "" (ic/list client))))))
