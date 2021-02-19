-- MySQL Workbench Forward Engineering

SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0;
SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0;
SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_ENGINE_SUBSTITUTION';

-- -----------------------------------------------------
-- Schema tcp_server
-- -----------------------------------------------------

-- -----------------------------------------------------
-- Schema tcp_server
-- -----------------------------------------------------
CREATE SCHEMA IF NOT EXISTS `tcp_server` DEFAULT CHARACTER SET utf8 ;
USE `tcp_server` ;

-- -----------------------------------------------------
-- Table `tcp_server`.`User`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `tcp_server`.`User` ;

CREATE TABLE IF NOT EXISTS `tcp_server`.`User` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `colorCode` VARCHAR(10) NULL,
  `password` VARCHAR(256) NULL,
  `userName` VARCHAR(45) NULL UNIQUE,
  PRIMARY KEY (`id`))
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `tcp_server`.`Message`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `tcp_server`.`Message` ;

CREATE TABLE IF NOT EXISTS `tcp_server`.`Message` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `content` VARCHAR(512) NULL,
  `author` INT NOT NULL,
  PRIMARY KEY (`id`),
  INDEX `fk_Message_User1_idx` (`author` ASC),
  CONSTRAINT `fk_Message_User1`
    FOREIGN KEY (`author`)
    REFERENCES `tcp_server`.`User` (`id`))
ENGINE = InnoDB;

-- -----------------------------------------------------
-- Table `tcp_server`.`ChatRoom`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `tcp_server`.`ChatRoom` ;

CREATE TABLE IF NOT EXISTS `tcp_server`.`ChatRoom` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `roomId` VARCHAR(45) NULL UNIQUE,
  `password` VARCHAR(256) NULL,
  PRIMARY KEY (`id`))
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `tcp_server`.`Messages`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `tcp_server`.`Messages` ;

CREATE TABLE IF NOT EXISTS `tcp_server`.`Messages` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `Message_id` INT NOT NULL,
  `ChatRoom_id` INT NOT NULL,
  INDEX `fk_Message_has_ChatRoom_ChatRoom1_idx` (`ChatRoom_id` ASC),
  INDEX `fk_Message_has_ChatRoom_Message_idx` (`Message_id` ASC),
  PRIMARY KEY (`id`),
  CONSTRAINT `fk_Message_has_ChatRoom_Message`
    FOREIGN KEY (`Message_id`)
    REFERENCES `tcp_server`.`Message` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_Message_has_ChatRoom_ChatRoom1`
    FOREIGN KEY (`ChatRoom_id`)
    REFERENCES `tcp_server`.`ChatRoom` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB;


SET SQL_MODE=@OLD_SQL_MODE;
SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS;
SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS;
