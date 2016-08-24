(ns nomad.migrator.postgres
  (:require
   [nomad.core :as nomad :refer [defmigration]]
   [nomad.migrator.core :as migrator]
   [clojure.java.jdbc :as jdbc]))

(defrecord PostgresStore [db-spec])

(defn connect [{:as opts :keys [db host port user password]}]
  (PostgresStore.
   {:subname (str "//" (or host "localhost")
                  ":" (or port 5432)
                  "/" db)
    :subprotocol "postgresql"
    :user user
    :password password
    :classname "org.postgresql.Driver"}))

(defn init [{:keys [db-spec]}]
  (jdbc/with-connection db-spec
    (jdbc/transaction
     (when (-> (jdbc/with-connection db-spec
                 (jdbc/with-query-results rset
                   [(str "SELECT * FROM information_schema.tables "
                         "WHERE table_schema = 'public' "
                         " AND table_name = 'nomad_schema_migrations'")]
                   (doall rset)))
               empty?)
       (jdbc/do-commands
        (str "CREATE TABLE nomad_schema_migrations"
             "(tag VARCHAR(256)"
             ",applied TIMESTAMP"
             ",CONSTRAINT uq_tag UNIQUE (tag))"))))))

(extend PostgresStore
  nomad/IMigrator
  {:-init init
   :-fini migrator/fini
   :-load-migrations migrator/load-migrations
   :-applied? migrator/applied?
   :-apply! migrator/apply!})
