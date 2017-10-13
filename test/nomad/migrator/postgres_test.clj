(ns nomad.migrator.postgres-test
  (:require
   [clojure.test :refer :all]
   [clojure.java.shell :as sh]
   [clojure.string :as str]
   [clojure.java.jdbc :as jdbc]
   [nomad.core :as nomad]
   [nomad.migrator.postgres :as postgres]))

(def db (atom nil))

(defn fixture [f]
  (let [db-name (str "nomad-test-" (gensym))
        opts    (System/getenv "PG_OPTS")
        opts    (when opts
                  (str/split opts #" "))]
    (try
      (apply sh/sh (vec (flatten (list "dropdb" opts db-name))))
      (apply sh/sh (vec (flatten (list "createdb" opts db-name))))
      (reset! db (postgres/connect {:db db-name
                                    :user "postgres"
                                    :password "postgres"
                                    :host "localhost"
                                    :post 5432}))
      (f)
      (finally
        (apply sh/sh (vec (flatten (list "dropdb" opts db-name))))))))

(use-fixtures :each fixture)

(deftest migrations
  (is (= {:index #{}, :clauses []} (nomad/clear-migrations!)))
  (is (= :ok
         (nomad/register-migration! "init-schema"
                                    {:up (fn []
                                           (jdbc/with-db-connection [conn (:db-spec @db)]
                                             (jdbc/db-do-commands
                                              conn
                                              "CREATE TABLE test1(name VARCHAR(32))")))})))
  (is (= :ok
         (nomad/register-migration! "add-test1-age"
                                    {:up (fn []
                                           (jdbc/with-db-connection [conn (:db-spec @db)]
                                             (jdbc/db-do-commands
                                              conn
                                              "ALTER TABLE test1 ADD COLUMN age INTEGER")))})))
  (is (= :ok (nomad/migrate! @db)))

  (is (= :ok
         (do
           (jdbc/with-db-connection [conn (:db-spec @db)]
             (jdbc/insert! conn :test1 {:name "foo" :age 42}))
           :ok)))

  (is (< 0
         (jdbc/with-db-connection [conn (:db-spec @db)]
           (-> (jdbc/query conn
                       ["SELECT COUNT(1) AS COUNT FROM test1"])
               first
               :count)))))
