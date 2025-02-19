dependencies {
    compileOnly(project(":"))

    // database driver
    compileOnly("org.xerial", "sqlite-jdbc", "3.42.0.0")
    compileOnly("org.postgresql", "postgresql", "42.6.0")
    compileOnly("org.mariadb.jdbc", "mariadb-java-client", "3.1.4")
    compileOnly("mysql", "mysql-connector-java", "8.0.33")
}
