(ns nomad.migrator.h2-test
  (:require
   [clojure.test :refer :all]
   [clojure.java.jdbc :as jdbc]
   [nomad.core :as nomad]
   [nomad.migrator.h2 :as h2]))

(defonce db (h2/connect {:db "mem:test;DB_CLOSE_DELAY=-1;IGNORECASE=TRUE"}))

(def db-spec (:db-spec db))

(deftest migrations
  (is (= {:index #{}, :clauses []} (nomad/clear-migrations!)))
  (is (= :ok
         (nomad/register-migration! "init-schema"
                                    {:up (fn [conn]
                                           (jdbc/db-do-commands
                                            conn
                                            "CREATE TABLE test1(name VARCHAR(32))"))})))
  (is (= :ok
         (nomad/register-migration! "add-test1-age"
                                    {:up (fn [conn]
                                           (jdbc/db-do-commands
                                            conn
                                            "ALTER TABLE test1 ADD COLUMN age INTEGER"))})))
  (is (= :ok (nomad/migrate! db)))

  (is (= :ok
         (do
           (jdbc/with-db-connection [conn db-spec]
             (jdbc/insert! conn :test1 {:name "foo" :age 42}))
           :ok)))

  (is (< 0
         (jdbc/with-db-connection [conn db-spec]
           (-> (jdbc/query conn
                       ["SELECT COUNT(1) AS COUNT FROM test1"])
               first
               :count)))))
