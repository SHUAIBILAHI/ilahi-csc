sudo apt-get update && sudo apt-get install -y mariadb-server
sudo service mariadb start
sudo mysql -e "CREATE DATABASE IF NOT EXISTS ilahi CHARACTER SET utf8mb4; ALTER USER 'root'@'localhost' IDENTIFIED VIA mysql_native_password USING PASSWORD('root'); FLUSH PRIVILEGES;"
