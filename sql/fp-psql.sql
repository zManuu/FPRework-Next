START TRANSACTION;

CREATE TABLE accounts (
  "id" int NOT NULL PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
  "playerUuid" varchar(36) NOT NULL,
  "name" varchar(50) NOT NULL,
  "password" varchar(200) DEFAULT NULL,
  "lastLogin" varchar(19) DEFAULT NULL
);

CREATE TABLE account_options (
  "id" int NOT NULL PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
  "accountId" int NOT NULL,
  "languageKey" varchar(3) NOT NULL,
  "buildMode" boolean NOT NULL
);

CREATE TABLE characters (
  "id" int NOT NULL PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
  "accountId" int NOT NULL,
  "name" varchar(50) NOT NULL,
  "locWorld" varchar(50) NOT NULL,
  "locX" real NOT NULL,
  "locY" real NOT NULL,
  "locZ" real NOT NULL,
  "locYaw" real NOT NULL,
  "locPitch" real NOT NULL,
  "active" boolean NOT NULL,
  "foodLevel" int NOT NULL,
  "characterClassIdentifier" varchar(50) NOT NULL
);

CREATE TABLE save_points (
  "id" int NOT NULL PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
  "characterId" int NOT NULL,
  "savePointId" int NOT NULL
);

COMMIT;
