(def VERSION (or (not-empty (System/getenv "SEMVER")) "0.0.1-SNAPSHOT"))

(defproject net.omnypay/nomad-migrators VERSION
  :encoding "utf-8"
  :description "OmnyPay Nomad migrators for H2 and PostgreSQL"
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [com.h2database/h2 "1.4.192"]
                 [net.omnypay/nomad "0.0.7"]
                 [org.clojure/java.jdbc "0.6.1"]
                 [org.postgresql/postgresql "9.4.1208"]])
