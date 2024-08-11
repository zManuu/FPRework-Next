SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
START TRANSACTION;
SET time_zone = "+00:00";

CREATE TABLE `accounts` (
  `id` int(11) NOT NULL PRIMARY KEY AUTO_INCREMENT,
  `playerUuid` varchar(36) NOT NULL,
  `name` varchar(50) NOT NULL,
  `password` varchar(200) DEFAULT NULL,
  `lastLogin` varchar(19) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin;

CREATE TABLE `account_options` (
  `id` int(11) NOT NULL PRIMARY KEY AUTO_INCREMENT,
  `accountId` int(11) NOT NULL,
  `languageKey` varchar(3) NOT NULL,
  `buildMode` tinyint(1) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin;

CREATE TABLE `characters` (
  `id` int(11) NOT NULL PRIMARY KEY AUTO_INCREMENT,
  `accountId` int(11) NOT NULL,
  `name` varchar(50) NOT NULL,
  `locWorld` varchar(50) NOT NULL,
  `locX` double NOT NULL,
  `locY` double NOT NULL,
  `locZ` double NOT NULL,
  `locYaw` float NOT NULL,
  `locPitch` float NOT NULL,
  `active` tinyint(1) NOT NULL,
  `foodLevel` int(11) NOT NULL,
  `characterClassIdentifier` varchar(50) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin;

CREATE TABLE `save_points` (
  `id` int(11) NOT NULL PRIMARY KEY AUTO_INCREMENT,
  `characterId` int(11) NOT NULL,
  `savePointId` int(11) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin;

CREATE TABLE `friends` (
  `id` int(11) NOT NULL PRIMARY KEY AUTO_INCREMENT,
  `accountId1` int(11) NOT NULL,
  `accountId2` int(11) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin;

CREATE TABLE `friend_requests` (
  `id` int(11) NOT NULL PRIMARY KEY AUTO_INCREMENT,
  `requestingAccountId` int(11) NOT NULL,
  `receivingAccountId` int(11) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin;

COMMIT;
