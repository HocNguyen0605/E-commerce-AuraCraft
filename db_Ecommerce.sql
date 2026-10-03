-- MySQL dump 10.13  Distrib 8.0.46, for Win64 (x86_64)
--
-- Host: 127.0.0.1    Database: handmade_marketplace
-- ------------------------------------------------------
-- Server version	8.0.46

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `account`
--
SET NAMES utf8mb4;
DROP DATABASE IF EXISTS handmade_marketplace;
CREATE DATABASE handmade_marketplace CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE handmade_marketplace;
DROP TABLE IF EXISTS `account`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `account` (
  `id` int unsigned NOT NULL AUTO_INCREMENT,
  `id_user` int unsigned NOT NULL,
  `password` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `role` enum('buyer','seller','admin') CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'buyer',
  `loyaltyPoint` int unsigned NOT NULL DEFAULT '0',
  `provider` enum('local','google') CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'local',
  `google_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `email_verified` tinyint(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  UNIQUE KEY `id_user` (`id_user`),
  UNIQUE KEY `google_id` (`google_id`),
  CONSTRAINT `fk_account_user` FOREIGN KEY (`id_user`) REFERENCES `users` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `account`
--

LOCK TABLES `account` WRITE;
/*!40000 ALTER TABLE `account` DISABLE KEYS */;
INSERT INTO `account` VALUES (1,1,'$2a$10$7EqJtq98hPqEX7fNZaFWoOa5Xo0u3m9vJ0pQyH1k2L3m4N5o6P7q8','admin',0,'local',NULL,1),(2,2,'$2a$10$7EqJtq98hPqEX7fNZaFWoOa5Xo0u3m9vJ0pQyH1k2L3m4N5o6P7q8','seller',0,'local',NULL,1),(3,3,'$2a$10$7EqJtq98hPqEX7fNZaFWoOa5Xo0u3m9vJ0pQyH1k2L3m4N5o6P7q8','seller',0,'local',NULL,1),(4,4,'$2a$10$7EqJtq98hPqEX7fNZaFWoOa5Xo0u3m9vJ0pQyH1k2L3m4N5o6P7q8','buyer',618,'local',NULL,1),(5,5,'$2a$10$7EqJtq98hPqEX7fNZaFWoOa5Xo0u3m9vJ0pQyH1k2L3m4N5o6P7q8','buyer',100,'local',NULL,1),(6,6,NULL,'buyer',0,'google','104512345678901234567',1);
/*!40000 ALTER TABLE `account` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `addresses`
--

DROP TABLE IF EXISTS `addresses`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `addresses` (
  `id` int unsigned NOT NULL AUTO_INCREMENT,
  `id_user` int unsigned NOT NULL,
  `receiver` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `address` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `is_default` tinyint(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `idx_addr_user` (`id_user`),
  CONSTRAINT `fk_addr_user` FOREIGN KEY (`id_user`) REFERENCES `users` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `addresses`
--

LOCK TABLES `addresses` WRITE;
/*!40000 ALTER TABLE `addresses` DISABLE KEYS */;
INSERT INTO `addresses` VALUES (1,4,'Pham Thi Dung','0901000004','12 Vo Van Ngan, Thu Duc, TP.HCM',1),(2,4,'Pham Thi Dung','0901000004','KTX Khu B, Dai hoc Nong Lam, Thu Duc, TP.HCM',0),(3,5,'Hoang Van Em','0901000005','45 Le Duan, Quan 1, TP.HCM',1),(4,6,'Vo Thi Phuong','0901000006','88 Nguyen Hue, Quan 1, TP.HCM',1);
/*!40000 ALTER TABLE `addresses` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `carts`
--

DROP TABLE IF EXISTS `carts`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `carts` (
  `id` int unsigned NOT NULL AUTO_INCREMENT,
  `id_user` int unsigned NOT NULL,
  `id_product` int unsigned DEFAULT NULL,
  `id_custom_design` int unsigned DEFAULT NULL,
  `quantity` int unsigned NOT NULL DEFAULT '1',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_cart_user_prod` (`id_user`,`id_product`),
  KEY `idx_cart_user` (`id_user`),
  KEY `fk_cart_prod` (`id_product`),
  KEY `fk_cart_design` (`id_custom_design`),
  CONSTRAINT `fk_cart_design` FOREIGN KEY (`id_custom_design`) REFERENCES `customdesigns` (`id`),
  CONSTRAINT `fk_cart_prod` FOREIGN KEY (`id_product`) REFERENCES `products` (`id`),
  CONSTRAINT `fk_cart_user` FOREIGN KEY (`id_user`) REFERENCES `users` (`id`) ON DELETE CASCADE,
  CONSTRAINT `chk_cart_item` CHECK ((((`id_product` is not null) + (`id_custom_design` is not null)) = 1)),
  CONSTRAINT `chk_cart_qty` CHECK ((`quantity` > 0))
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `carts`
--

LOCK TABLES `carts` WRITE;
/*!40000 ALTER TABLE `carts` DISABLE KEYS */;
INSERT INTO `carts` VALUES (1,4,6,NULL,1),(2,4,NULL,1,1),(3,5,4,NULL,2),(4,5,NULL,2,1);
/*!40000 ALTER TABLE `carts` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `categories`
--

DROP TABLE IF EXISTS `categories`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `categories` (
  `id` int unsigned NOT NULL AUTO_INCREMENT,
  `name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `name` (`name`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `categories`
--

LOCK TABLES `categories` WRITE;
/*!40000 ALTER TABLE `categories` DISABLE KEYS */;
INSERT INTO `categories` VALUES (3,'Charm dien thoai'),(2,'Day chuyen'),(1,'Vong tay');
/*!40000 ALTER TABLE `categories` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `chatmessages`
--

DROP TABLE IF EXISTS `chatmessages`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `chatmessages` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `id_session` int unsigned NOT NULL,
  `role` enum('user','assistant') CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_chatm_session` (`id_session`),
  CONSTRAINT `fk_chatm_session` FOREIGN KEY (`id_session`) REFERENCES `chatsessions` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `chatmessages`
--

LOCK TABLES `chatmessages` WRITE;
/*!40000 ALTER TABLE `chatmessages` DISABLE KEYS */;
INSERT INTO `chatmessages` VALUES (1,1,'user','Goi y giup minh mau hat hop voi vong tay mau do','2026-09-24 20:00:10'),(2,1,'assistant','Ban co the ket hop hat da thach anh trang voi hat go do.','2026-09-24 20:00:15'),(3,2,'user','Charm dien thoai lam mat bao lau?','2026-09-27 21:30:05'),(4,2,'assistant','Thuong tu 3 den 7 ngay tuy tho nhan lam.','2026-09-27 21:30:09');
/*!40000 ALTER TABLE `chatmessages` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `chatsessions`
--

DROP TABLE IF EXISTS `chatsessions`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `chatsessions` (
  `id` int unsigned NOT NULL AUTO_INCREMENT,
  `id_user` int unsigned NOT NULL,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_chats_user` (`id_user`),
  CONSTRAINT `fk_chats_user` FOREIGN KEY (`id_user`) REFERENCES `users` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `chatsessions`
--

LOCK TABLES `chatsessions` WRITE;
/*!40000 ALTER TABLE `chatsessions` DISABLE KEYS */;
INSERT INTO `chatsessions` VALUES (1,4,'2026-09-24 20:00:00'),(2,5,'2026-09-27 21:30:00');
/*!40000 ALTER TABLE `chatsessions` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `customdesigns`
--

DROP TABLE IF EXISTS `customdesigns`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `customdesigns` (
  `id` int unsigned NOT NULL AUTO_INCREMENT,
  `id_user` int unsigned NOT NULL,
  `id_template` int unsigned NOT NULL,
  `config` json NOT NULL,
  `total_price` decimal(12,0) NOT NULL,
  `preview_image` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_cdes_user` (`id_user`),
  KEY `idx_cdes_tpl` (`id_template`),
  CONSTRAINT `fk_cdes_tpl` FOREIGN KEY (`id_template`) REFERENCES `designtemplates` (`id`),
  CONSTRAINT `fk_cdes_user` FOREIGN KEY (`id_user`) REFERENCES `users` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `customdesigns`
--

LOCK TABLES `customdesigns` WRITE;
/*!40000 ALTER TABLE `customdesigns` DISABLE KEYS */;
INSERT INTO `customdesigns` VALUES (1,4,1,'{\"beads\": [\"Hat da thach anh\", \"Hat go do\"], \"charm\": \"Charm trai tim\", \"length_cm\": 16}',200000,'previews/d1.png'),(2,5,2,'{\"color\": \"Mau hong\", \"material\": \"Dat set\"}',90000,'previews/d2.png');
/*!40000 ALTER TABLE `customdesigns` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `customrequests`
--

DROP TABLE IF EXISTS `customrequests`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `customrequests` (
  `id` int unsigned NOT NULL AUTO_INCREMENT,
  `id_user` int unsigned NOT NULL,
  `id_design` int unsigned NOT NULL,
  `budget` decimal(12,0) DEFAULT NULL,
  `deadline` date DEFAULT NULL,
  `status` enum('open','assigned','closed') CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'open',
  `date_create` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_creq_user` (`id_user`),
  KEY `idx_creq_status` (`status`),
  KEY `fk_creq_design` (`id_design`),
  CONSTRAINT `fk_creq_design` FOREIGN KEY (`id_design`) REFERENCES `customdesigns` (`id`),
  CONSTRAINT `fk_creq_user` FOREIGN KEY (`id_user`) REFERENCES `users` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `customrequests`
--

LOCK TABLES `customrequests` WRITE;
/*!40000 ALTER TABLE `customrequests` DISABLE KEYS */;
INSERT INTO `customrequests` VALUES (1,4,1,250000,'2026-10-20','assigned','2026-09-25 09:00:00'),(2,5,2,100000,'2026-10-25','open','2026-09-28 14:30:00');
/*!40000 ALTER TABLE `customrequests` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `designoptions`
--

DROP TABLE IF EXISTS `designoptions`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `designoptions` (
  `id` int unsigned NOT NULL AUTO_INCREMENT,
  `id_template` int unsigned NOT NULL,
  `type` enum('bead','charm','color','material','length') CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `price_delta` decimal(12,0) NOT NULL DEFAULT '0',
  `image` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_dopt_tpl` (`id_template`),
  CONSTRAINT `fk_dopt_tpl` FOREIGN KEY (`id_template`) REFERENCES `designtemplates` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `designoptions`
--

LOCK TABLES `designoptions` WRITE;
/*!40000 ALTER TABLE `designoptions` DISABLE KEYS */;
INSERT INTO `designoptions` VALUES (1,1,'bead','Hat go do',0,'opt/bead-red.jpg'),(2,1,'bead','Hat da thach anh',30000,'opt/bead-quartz.jpg'),(3,1,'charm','Charm trai tim',20000,'opt/charm-heart.jpg'),(4,1,'length','16 cm',0,NULL),(5,2,'color','Mau hong',0,NULL),(6,2,'material','Dat set',10000,NULL);
/*!40000 ALTER TABLE `designoptions` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `designtemplates`
--

DROP TABLE IF EXISTS `designtemplates`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `designtemplates` (
  `id` int unsigned NOT NULL AUTO_INCREMENT,
  `id_product` int unsigned NOT NULL,
  `name` varchar(150) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `base_price` decimal(12,0) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_dtpl_prod` (`id_product`),
  CONSTRAINT `fk_dtpl_prod` FOREIGN KEY (`id_product`) REFERENCES `products` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `designtemplates`
--

LOCK TABLES `designtemplates` WRITE;
/*!40000 ALTER TABLE `designtemplates` DISABLE KEYS */;
INSERT INTO `designtemplates` VALUES (1,3,'Vong tay hat tuy chinh',150000),(2,5,'Charm dien thoai tuy chinh',80000);
/*!40000 ALTER TABLE `designtemplates` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `emailverification`
--

DROP TABLE IF EXISTS `emailverification`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `emailverification` (
  `id` int unsigned NOT NULL AUTO_INCREMENT,
  `id_account` int unsigned NOT NULL,
  `token` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `expires_at` datetime NOT NULL,
  `used` tinyint(1) NOT NULL DEFAULT '0',
  `type` enum('verify','reset') CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `token` (`token`),
  KEY `fk_emailver_account` (`id_account`),
  CONSTRAINT `fk_emailver_account` FOREIGN KEY (`id_account`) REFERENCES `account` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `emailverification`
--

LOCK TABLES `emailverification` WRITE;
/*!40000 ALTER TABLE `emailverification` DISABLE KEYS */;
INSERT INTO `emailverification` VALUES (1,4,'verify-token-0004-abcdef','2026-09-01 10:00:00',1,'verify'),(2,5,'verify-token-0005-ghijkl','2026-09-05 10:00:00',1,'verify'),(3,4,'reset-token-0004-mnopqr','2026-10-10 10:00:00',0,'reset');
/*!40000 ALTER TABLE `emailverification` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `loyaltytransactions`
--

DROP TABLE IF EXISTS `loyaltytransactions`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `loyaltytransactions` (
  `id` int unsigned NOT NULL AUTO_INCREMENT,
  `id_user` int unsigned NOT NULL,
  `id_order` int unsigned DEFAULT NULL,
  `points` int NOT NULL,
  `type` enum('earn','redeem','adjust') CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `date` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_loy_user` (`id_user`),
  KEY `fk_loy_order` (`id_order`),
  CONSTRAINT `fk_loy_order` FOREIGN KEY (`id_order`) REFERENCES `orders` (`id`) ON DELETE SET NULL,
  CONSTRAINT `fk_loy_user` FOREIGN KEY (`id_user`) REFERENCES `users` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `loyaltytransactions`
--

LOCK TABLES `loyaltytransactions` WRITE;
/*!40000 ALTER TABLE `loyaltytransactions` DISABLE KEYS */;
INSERT INTO `loyaltytransactions` VALUES (1,4,1,408,'earn','2026-09-20 10:20:00'),(2,4,3,210,'earn','2026-09-30 15:05:00'),(3,5,NULL,100,'adjust','2026-09-10 08:00:00');
/*!40000 ALTER TABLE `loyaltytransactions` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `orderitems`
--

DROP TABLE IF EXISTS `orderitems`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `orderitems` (
  `id` int unsigned NOT NULL AUTO_INCREMENT,
  `id_order` int unsigned NOT NULL,
  `id_product` int unsigned DEFAULT NULL,
  `id_custom_design` int unsigned DEFAULT NULL,
  `id_quote` int unsigned DEFAULT NULL,
  `quantity` int unsigned NOT NULL DEFAULT '1',
  `price` decimal(12,0) NOT NULL,
  `fee_amount` decimal(12,0) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `idx_oitem_order` (`id_order`),
  KEY `idx_oitem_prod` (`id_product`),
  KEY `fk_oitem_design` (`id_custom_design`),
  KEY `fk_oitem_quote` (`id_quote`),
  CONSTRAINT `fk_oitem_design` FOREIGN KEY (`id_custom_design`) REFERENCES `customdesigns` (`id`),
  CONSTRAINT `fk_oitem_order` FOREIGN KEY (`id_order`) REFERENCES `orders` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_oitem_prod` FOREIGN KEY (`id_product`) REFERENCES `products` (`id`),
  CONSTRAINT `fk_oitem_quote` FOREIGN KEY (`id_quote`) REFERENCES `quotes` (`id`),
  CONSTRAINT `chk_oitem_item` CHECK ((((`id_product` is not null) + (`id_custom_design` is not null)) = 1)),
  CONSTRAINT `chk_oitem_qty` CHECK ((`quantity` > 0))
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `orderitems`
--

LOCK TABLES `orderitems` WRITE;
/*!40000 ALTER TABLE `orderitems` DISABLE KEYS */;
INSERT INTO `orderitems` VALUES (1,1,1,NULL,NULL,2,120000,12000),(2,1,2,NULL,NULL,1,180000,9000),(3,2,4,NULL,NULL,2,60000,9600),(4,3,NULL,1,1,1,200000,10000);
/*!40000 ALTER TABLE `orderitems` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `orders`
--

DROP TABLE IF EXISTS `orders`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `orders` (
  `id` int unsigned NOT NULL AUTO_INCREMENT,
  `id_user` int unsigned NOT NULL,
  `id_payment_method` int unsigned NOT NULL,
  `id_shop` int unsigned NOT NULL,
  `amount` decimal(12,0) NOT NULL,
  `date_create` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `date_paid` datetime DEFAULT NULL,
  `id_voucher` int unsigned DEFAULT NULL,
  `status` enum('pending','paid','processing','shipping','completed','cancelled','refunded') CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'pending',
  `shipping_fee` decimal(12,0) NOT NULL DEFAULT '0',
  `discount` decimal(12,0) NOT NULL DEFAULT '0',
  `points_used` int unsigned NOT NULL DEFAULT '0',
  `shipping_address` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_order_user` (`id_user`),
  KEY `idx_order_shop` (`id_shop`),
  KEY `idx_order_paid` (`date_paid`),
  KEY `idx_order_status` (`status`),
  KEY `fk_order_payment` (`id_payment_method`),
  KEY `fk_order_voucher` (`id_voucher`),
  CONSTRAINT `fk_order_payment` FOREIGN KEY (`id_payment_method`) REFERENCES `paymentmethods` (`id`),
  CONSTRAINT `fk_order_shop` FOREIGN KEY (`id_shop`) REFERENCES `shops` (`id`),
  CONSTRAINT `fk_order_user` FOREIGN KEY (`id_user`) REFERENCES `users` (`id`),
  CONSTRAINT `fk_order_voucher` FOREIGN KEY (`id_voucher`) REFERENCES `vouchers` (`id`) ON DELETE SET NULL
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `orders`
--

LOCK TABLES `orders` WRITE;
/*!40000 ALTER TABLE `orders` DISABLE KEYS */;
INSERT INTO `orders` VALUES (1,4,2,1,408000,'2026-09-20 10:15:00','2026-09-20 10:20:00',1,'completed',30000,42000,0,'12 Vo Van Ngan, Thu Duc, TP.HCM'),(2,5,1,2,145000,'2026-10-01 09:00:00',NULL,NULL,'shipping',25000,0,0,'45 Le Duan, Quan 1, TP.HCM'),(3,4,3,1,210000,'2026-09-30 15:00:00','2026-09-30 15:05:00',2,'processing',30000,20000,0,'12 Vo Van Ngan, Thu Duc, TP.HCM');
/*!40000 ALTER TABLE `orders` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `paymentmethods`
--

DROP TABLE IF EXISTS `paymentmethods`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `paymentmethods` (
  `id` int unsigned NOT NULL AUTO_INCREMENT,
  `name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `name` (`name`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `paymentmethods`
--

LOCK TABLES `paymentmethods` WRITE;
/*!40000 ALTER TABLE `paymentmethods` DISABLE KEYS */;
INSERT INTO `paymentmethods` VALUES (1,'COD'),(3,'MoMo'),(2,'VNPay');
/*!40000 ALTER TABLE `paymentmethods` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `productimages`
--

DROP TABLE IF EXISTS `productimages`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `productimages` (
  `id` int unsigned NOT NULL AUTO_INCREMENT,
  `id_product` int unsigned NOT NULL,
  `url` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_pimg_prod` (`id_product`),
  CONSTRAINT `fk_pimg_prod` FOREIGN KEY (`id_product`) REFERENCES `products` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=8 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `productimages`
--

LOCK TABLES `productimages` WRITE;
/*!40000 ALTER TABLE `productimages` DISABLE KEYS */;
INSERT INTO `productimages` VALUES (1,1,'products/vong-go-1.jpg'),(2,1,'products/vong-go-2.jpg'),(3,2,'products/day-voso-1.jpg'),(4,3,'products/vong-custom-1.jpg'),(5,4,'products/charm-gau-1.jpg'),(6,5,'products/charm-hoa-1.jpg'),(7,6,'products/day-ngoctrai-1.jpg');
/*!40000 ALTER TABLE `productimages` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `products`
--

DROP TABLE IF EXISTS `products`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `products` (
  `id` int unsigned NOT NULL AUTO_INCREMENT,
  `name` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `price` decimal(12,0) NOT NULL,
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci,
  `id_shop` int unsigned NOT NULL,
  `stock` int unsigned NOT NULL DEFAULT '0',
  `id_category` int unsigned NOT NULL,
  `status` enum('active','hidden','deleted') CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'active',
  `sold_count` int unsigned NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `idx_prod_shop` (`id_shop`),
  KEY `idx_prod_cat` (`id_category`),
  KEY `idx_prod_sold` (`sold_count`),
  CONSTRAINT `fk_prod_cat` FOREIGN KEY (`id_category`) REFERENCES `categories` (`id`),
  CONSTRAINT `fk_prod_shop` FOREIGN KEY (`id_shop`) REFERENCES `shops` (`id`),
  CONSTRAINT `chk_prod_price` CHECK ((`price` >= 0))
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `products`
--

LOCK TABLES `products` WRITE;
/*!40000 ALTER TABLE `products` DISABLE KEYS */;
INSERT INTO `products` VALUES (1,'Vong tay hat go tu nhien',120000,'Vong tay hat go thom, size 16-18cm',1,30,1,'active',15),(2,'Day chuyen vo so bien',180000,'Day chuyen vo so, day da the chinh duoc',1,20,2,'active',9),(3,'Vong tay hat tu chon',150000,'Vong tay tuy chinh mau hat va charm',1,100,1,'active',4),(4,'Charm dien thoai gau',60000,'Charm gau bong handmade',2,50,3,'active',22),(5,'Charm dien thoai hoa',55000,'Charm hoa dat set',2,40,3,'active',11),(6,'Day chuyen ngoc trai',250000,'Ngoc trai nuoc ngot, khoa bac',2,12,2,'active',6);
/*!40000 ALTER TABLE `products` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `quotes`
--

DROP TABLE IF EXISTS `quotes`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `quotes` (
  `id` int unsigned NOT NULL AUTO_INCREMENT,
  `id_request` int unsigned NOT NULL,
  `id_shop` int unsigned NOT NULL,
  `price` decimal(12,0) NOT NULL,
  `lead_time_days` smallint unsigned NOT NULL,
  `note` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci,
  `status` enum('pending','accepted','rejected') CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'pending',
  `date` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_quote_req_shop` (`id_request`,`id_shop`),
  KEY `idx_quote_shop` (`id_shop`),
  CONSTRAINT `fk_quote_req` FOREIGN KEY (`id_request`) REFERENCES `customrequests` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_quote_shop` FOREIGN KEY (`id_shop`) REFERENCES `shops` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `quotes`
--

LOCK TABLES `quotes` WRITE;
/*!40000 ALTER TABLE `quotes` DISABLE KEYS */;
INSERT INTO `quotes` VALUES (1,1,1,200000,5,'Lam bang hat da that, giao trong 5 ngay','accepted','2026-09-26 10:00:00'),(2,1,2,230000,7,'Co the lam nhung can 7 ngay','rejected','2026-09-26 11:00:00'),(3,2,2,90000,3,'Nhan lam charm hong dat set','pending','2026-09-29 08:00:00');
/*!40000 ALTER TABLE `quotes` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `reviews`
--

DROP TABLE IF EXISTS `reviews`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `reviews` (
  `id` int unsigned NOT NULL AUTO_INCREMENT,
  `id_user` int unsigned NOT NULL,
  `id_product` int unsigned NOT NULL,
  `id_order` int unsigned NOT NULL,
  `rating` tinyint unsigned NOT NULL,
  `comment` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci,
  `date` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_review` (`id_user`,`id_product`,`id_order`),
  KEY `idx_review_prod` (`id_product`),
  KEY `fk_review_order` (`id_order`),
  CONSTRAINT `fk_review_order` FOREIGN KEY (`id_order`) REFERENCES `orders` (`id`),
  CONSTRAINT `fk_review_prod` FOREIGN KEY (`id_product`) REFERENCES `products` (`id`),
  CONSTRAINT `fk_review_user` FOREIGN KEY (`id_user`) REFERENCES `users` (`id`),
  CONSTRAINT `chk_review_rating` CHECK ((`rating` between 1 and 5))
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `reviews`
--

LOCK TABLES `reviews` WRITE;
/*!40000 ALTER TABLE `reviews` DISABLE KEYS */;
INSERT INTO `reviews` VALUES (1,4,1,1,5,'Vong dep, hat go thom, dong goi can than','2026-09-25 18:00:00'),(2,4,2,1,4,'Day chuyen dep nhung hoi dai so voi mong doi','2026-09-25 18:05:00');
/*!40000 ALTER TABLE `reviews` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `shops`
--

DROP TABLE IF EXISTS `shops`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `shops` (
  `id` int unsigned NOT NULL AUTO_INCREMENT,
  `id_owner` int unsigned NOT NULL,
  `name` varchar(150) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `address` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `email` varchar(150) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci,
  `avatar` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `status` enum('pending','active','locked') CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'pending',
  `rating` decimal(2,1) NOT NULL DEFAULT '0.0',
  `commission_rate` decimal(5,2) NOT NULL DEFAULT '5.00',
  PRIMARY KEY (`id`),
  KEY `idx_shop_owner` (`id_owner`),
  KEY `idx_shop_name` (`name`),
  CONSTRAINT `fk_shop_owner` FOREIGN KEY (`id_owner`) REFERENCES `users` (`id`),
  CONSTRAINT `chk_shop_commission` CHECK ((`commission_rate` between 0 and 100))
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `shops`
--

LOCK TABLES `shops` WRITE;
/*!40000 ALTER TABLE `shops` DISABLE KEYS */;
INSERT INTO `shops` VALUES (1,2,'Binh Handmade','20 Tran Hung Dao, Quan 5, TP.HCM','0901000002','binh.shop@gmail.com','Vong tay, day chuyen thu cong tu hat go va vo so','shops/binh.jpg','active',4.8,5.00),(2,3,'Cuong Craft','77 Cach Mang Thang 8, Quan 3, TP.HCM','0901000003','cuong.craft@gmail.com','Charm dien thoai va phu kien handmade','shops/cuong.jpg','active',4.6,8.00);
/*!40000 ALTER TABLE `shops` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `transactions`
--

DROP TABLE IF EXISTS `transactions`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `transactions` (
  `id` int unsigned NOT NULL AUTO_INCREMENT,
  `id_order` int unsigned NOT NULL,
  `gateway` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `txn_ref` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `amount` decimal(12,0) NOT NULL,
  `status` enum('pending','success','failed','refunded') CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'pending',
  `date` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_txn_gateway_ref` (`gateway`,`txn_ref`),
  KEY `idx_txn_order` (`id_order`),
  CONSTRAINT `fk_txn_order` FOREIGN KEY (`id_order`) REFERENCES `orders` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `transactions`
--

LOCK TABLES `transactions` WRITE;
/*!40000 ALTER TABLE `transactions` DISABLE KEYS */;
INSERT INTO `transactions` VALUES (1,1,'vnpay','VNP20260920101500',408000,'success','2026-09-20 10:20:00'),(2,2,'cod','COD-2',145000,'pending','2026-10-01 09:00:00'),(3,3,'momo','MOMO20260930150000',210000,'success','2026-09-30 15:05:00');
/*!40000 ALTER TABLE `transactions` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `users`
--

DROP TABLE IF EXISTS `users`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `users` (
  `id` int unsigned NOT NULL AUTO_INCREMENT,
  `name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `birthday` date DEFAULT NULL,
  `phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `email` varchar(150) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `email` (`email`)
) ENGINE=InnoDB AUTO_INCREMENT=13 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `users`
--

LOCK TABLES `users` WRITE;
/*!40000 ALTER TABLE `users` DISABLE KEYS */;
INSERT INTO `users` VALUES (1,'Nguyen Van An','1995-03-12','0901000001','admin@handmade.vn'),(2,'Tran Thi Binh','1992-07-25','0901000002','binh.shop@gmail.com'),(3,'Le Van Cuong','1990-11-02','0901000003','cuong.craft@gmail.com'),(4,'Pham Thi Dung','2003-01-18','0901000004','dung.buyer@gmail.com'),(5,'Hoang Van Em','2002-09-09','0901000005','em.buyer@gmail.com'),(6,'Vo Thi Phuong','2004-05-30','0901000006','phuong.buyer@gmail.com');
/*!40000 ALTER TABLE `users` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `vouchers`
--

DROP TABLE IF EXISTS `vouchers`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `vouchers` (
  `id` int unsigned NOT NULL AUTO_INCREMENT,
  `code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `discount_type` enum('percent','fixed') CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `value` decimal(12,0) NOT NULL,
  `min_order` decimal(12,0) NOT NULL DEFAULT '0',
  `start_date` datetime NOT NULL,
  `end_date` datetime NOT NULL,
  `quota` int unsigned NOT NULL DEFAULT '0',
  `id_shop` int unsigned DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `code` (`code`),
  KEY `idx_voucher_shop` (`id_shop`),
  CONSTRAINT `fk_voucher_shop` FOREIGN KEY (`id_shop`) REFERENCES `shops` (`id`) ON DELETE CASCADE,
  CONSTRAINT `chk_voucher_date` CHECK ((`end_date` >= `start_date`)),
  CONSTRAINT `chk_voucher_value` CHECK ((`value` > 0))
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `vouchers`
--

LOCK TABLES `vouchers` WRITE;
/*!40000 ALTER TABLE `vouchers` DISABLE KEYS */;
INSERT INTO `vouchers` VALUES (1,'SALE10','percent',10,200000,'2026-10-01 00:00:00','2026-12-31 23:59:59',100,NULL),(2,'BINH20K','fixed',20000,150000,'2026-10-01 00:00:00','2026-12-31 23:59:59',50,1);
/*!40000 ALTER TABLE `vouchers` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `voucherusage`
--

DROP TABLE IF EXISTS `voucherusage`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `voucherusage` (
  `id` int unsigned NOT NULL AUTO_INCREMENT,
  `id_voucher` int unsigned NOT NULL,
  `id_user` int unsigned NOT NULL,
  `id_order` int unsigned NOT NULL,
  `date` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `id_order` (`id_order`),
  KEY `idx_vu_voucher_user` (`id_voucher`,`id_user`),
  KEY `fk_vu_user` (`id_user`),
  CONSTRAINT `fk_vu_order` FOREIGN KEY (`id_order`) REFERENCES `orders` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_vu_user` FOREIGN KEY (`id_user`) REFERENCES `users` (`id`),
  CONSTRAINT `fk_vu_voucher` FOREIGN KEY (`id_voucher`) REFERENCES `vouchers` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `voucherusage`
--

LOCK TABLES `voucherusage` WRITE;
/*!40000 ALTER TABLE `voucherusage` DISABLE KEYS */;
INSERT INTO `voucherusage` VALUES (1,1,4,1,'2026-09-20 10:15:00'),(2,2,4,3,'2026-09-30 15:00:00');
/*!40000 ALTER TABLE `voucherusage` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-10-03 22:59:20
