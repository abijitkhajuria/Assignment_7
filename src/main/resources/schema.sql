-- ============================================================================
-- CricPulse — Real-Time Cricket Score Management System
-- MySQL 8.0 Relational Database Schema DDL Reference
-- ============================================================================

CREATE DATABASE IF NOT EXISTS `cricpulse_db` CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE `cricpulse_db`;

-- 1. Teams Table
CREATE TABLE IF NOT EXISTS `teams` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `name` VARCHAR(100) NOT NULL UNIQUE,
    `short_name` VARCHAR(10) NOT NULL UNIQUE,
    `logo_url` VARCHAR(255),
    `primary_color` VARCHAR(20),
    `created_at` DATETIME NOT NULL,
    `updated_at` DATETIME
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 2. Players Table
CREATE TABLE IF NOT EXISTS `players` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `name` VARCHAR(100) NOT NULL,
    `team_id` BIGINT NOT NULL,
    `role` VARCHAR(30) NOT NULL,
    `batting_style` VARCHAR(50),
    `bowling_style` VARCHAR(50),
    `jersey_number` INT,
    `career_matches` INT DEFAULT 0,
    `career_runs` INT DEFAULT 0,
    `career_highest_score` INT DEFAULT 0,
    `career_batting_average` DOUBLE DEFAULT 0.0,
    `career_strike_rate` DOUBLE DEFAULT 0.0,
    `career_wickets` INT DEFAULT 0,
    `career_bowling_average` DOUBLE DEFAULT 0.0,
    `career_best_bowling` VARCHAR(20) DEFAULT '-',
    `career_economy` DOUBLE DEFAULT 0.0,
    `avatar_url` VARCHAR(255),
    `created_at` DATETIME NOT NULL,
    `updated_at` DATETIME,
    CONSTRAINT `fk_player_team` FOREIGN KEY (`team_id`) REFERENCES `teams`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 3. Matches Table
CREATE TABLE IF NOT EXISTS `matches` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `title` VARCHAR(150) NOT NULL,
    `series_name` VARCHAR(150),
    `day_note` VARCHAR(150),
    `venue` VARCHAR(100) NOT NULL,
    `match_date` DATETIME NOT NULL,
    `format` VARCHAR(20) NOT NULL DEFAULT 'T20',
    `total_overs` INT NOT NULL DEFAULT 20,
    `team1_id` BIGINT NOT NULL,
    `team2_id` BIGINT NOT NULL,
    `toss_winner_id` BIGINT,
    `toss_decision` VARCHAR(10),
    `status` VARCHAR(30) NOT NULL DEFAULT 'SCHEDULED',
    `current_innings_number` INT DEFAULT 1,
    `winner_team_id` BIGINT,
    `result_margin` VARCHAR(150),
    `player_of_match_id` BIGINT,
    `match_summary` TEXT,
    `created_at` DATETIME NOT NULL,
    `updated_at` DATETIME,
    CONSTRAINT `fk_match_team1` FOREIGN KEY (`team1_id`) REFERENCES `teams`(`id`),
    CONSTRAINT `fk_match_team2` FOREIGN KEY (`team2_id`) REFERENCES `teams`(`id`),
    CONSTRAINT `fk_match_toss_winner` FOREIGN KEY (`toss_winner_id`) REFERENCES `teams`(`id`),
    CONSTRAINT `fk_match_winner` FOREIGN KEY (`winner_team_id`) REFERENCES `teams`(`id`),
    CONSTRAINT `fk_match_potm` FOREIGN KEY (`player_of_match_id`) REFERENCES `players`(`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 4. Innings Table
CREATE TABLE IF NOT EXISTS `innings` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `match_id` BIGINT NOT NULL,
    `innings_number` INT NOT NULL,
    `batting_team_id` BIGINT NOT NULL,
    `bowling_team_id` BIGINT NOT NULL,
    `total_runs` INT DEFAULT 0,
    `wickets` INT DEFAULT 0,
    `legal_balls` INT DEFAULT 0,
    `extras_wide` INT DEFAULT 0,
    `extras_no_ball` INT DEFAULT 0,
    `extras_bye` INT DEFAULT 0,
    `extras_leg_bye` INT DEFAULT 0,
    `extras_penalty` INT DEFAULT 0,
    `target_runs` INT,
    `current_striker_id` BIGINT,
    `current_non_striker_id` BIGINT,
    `current_bowler_id` BIGINT,
    `is_completed` BOOLEAN DEFAULT FALSE,
    `created_at` DATETIME NOT NULL,
    `updated_at` DATETIME,
    CONSTRAINT `fk_innings_match` FOREIGN KEY (`match_id`) REFERENCES `matches`(`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_innings_bat_team` FOREIGN KEY (`batting_team_id`) REFERENCES `teams`(`id`),
    CONSTRAINT `fk_innings_bowl_team` FOREIGN KEY (`bowling_team_id`) REFERENCES `teams`(`id`),
    CONSTRAINT `fk_innings_striker` FOREIGN KEY (`current_striker_id`) REFERENCES `players`(`id`),
    CONSTRAINT `fk_innings_non_striker` FOREIGN KEY (`current_non_striker_id`) REFERENCES `players`(`id`),
    CONSTRAINT `fk_innings_bowler` FOREIGN KEY (`current_bowler_id`) REFERENCES `players`(`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 5. Deliveries (Ball-by-Ball Events) Table
CREATE TABLE IF NOT EXISTS `deliveries` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `innings_id` BIGINT NOT NULL,
    `over_number` INT NOT NULL,
    `ball_number_in_over` INT NOT NULL,
    `legal_ball_number` INT,
    `striker_id` BIGINT NOT NULL,
    `non_striker_id` BIGINT NOT NULL,
    `bowler_id` BIGINT NOT NULL,
    `runs_batter` INT NOT NULL DEFAULT 0,
    `runs_extra` INT NOT NULL DEFAULT 0,
    `extra_type` VARCHAR(20) NOT NULL DEFAULT 'NONE',
    `is_legal_delivery` BOOLEAN NOT NULL DEFAULT TRUE,
    `is_wicket` BOOLEAN NOT NULL DEFAULT FALSE,
    `wicket_type` VARCHAR(30) DEFAULT 'NONE',
    `dismissed_player_id` BIGINT,
    `fielder_id` BIGINT,
    `commentary` VARCHAR(255),
    `team_runs_at_ball` INT,
    `wickets_at_ball` INT,
    `overs_at_ball_formatted` VARCHAR(15),
    `sequence_order` INT NOT NULL,
    `timestamp` DATETIME NOT NULL,
    INDEX `idx_delivery_innings_seq` (`innings_id`, `sequence_order`),
    CONSTRAINT `fk_delivery_innings` FOREIGN KEY (`innings_id`) REFERENCES `innings`(`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_delivery_striker` FOREIGN KEY (`striker_id`) REFERENCES `players`(`id`),
    CONSTRAINT `fk_delivery_non_striker` FOREIGN KEY (`non_striker_id`) REFERENCES `players`(`id`),
    CONSTRAINT `fk_delivery_bowler` FOREIGN KEY (`bowler_id`) REFERENCES `players`(`id`),
    CONSTRAINT `fk_delivery_dismissed` FOREIGN KEY (`dismissed_player_id`) REFERENCES `players`(`id`),
    CONSTRAINT `fk_delivery_fielder` FOREIGN KEY (`fielder_id`) REFERENCES `players`(`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 6. Batting Scores Table
CREATE TABLE IF NOT EXISTS `batting_scores` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `innings_id` BIGINT NOT NULL,
    `player_id` BIGINT NOT NULL,
    `batting_order` INT,
    `runs` INT DEFAULT 0,
    `balls_faced` INT DEFAULT 0,
    `fours` INT DEFAULT 0,
    `sixes` INT DEFAULT 0,
    `strike_rate` DOUBLE DEFAULT 0.0,
    `is_out` BOOLEAN DEFAULT FALSE,
    `wicket_type` VARCHAR(30) DEFAULT 'NONE',
    `bowler_id` BIGINT,
    `fielder_id` BIGINT,
    `dismissal_text` VARCHAR(150) DEFAULT 'not out',
    `created_at` DATETIME NOT NULL,
    `updated_at` DATETIME,
    CONSTRAINT `fk_batting_innings` FOREIGN KEY (`innings_id`) REFERENCES `innings`(`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_batting_player` FOREIGN KEY (`player_id`) REFERENCES `players`(`id`),
    CONSTRAINT `fk_batting_bowler` FOREIGN KEY (`bowler_id`) REFERENCES `players`(`id`),
    CONSTRAINT `fk_batting_fielder` FOREIGN KEY (`fielder_id`) REFERENCES `players`(`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 7. Bowling Scores Table
CREATE TABLE IF NOT EXISTS `bowling_scores` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `innings_id` BIGINT NOT NULL,
    `player_id` BIGINT NOT NULL,
    `legal_balls_bowled` INT DEFAULT 0,
    `maidens` INT DEFAULT 0,
    `runs_conceded` INT DEFAULT 0,
    `wickets` INT DEFAULT 0,
    `economy` DOUBLE DEFAULT 0.0,
    `created_at` DATETIME NOT NULL,
    `updated_at` DATETIME,
    CONSTRAINT `fk_bowling_innings` FOREIGN KEY (`innings_id`) REFERENCES `innings`(`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_bowling_player` FOREIGN KEY (`player_id`) REFERENCES `players`(`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 8. Fall of Wickets Table
CREATE TABLE IF NOT EXISTS `fall_of_wickets` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `innings_id` BIGINT NOT NULL,
    `wicket_number` INT NOT NULL,
    `team_score` INT NOT NULL,
    `overs_formatted` VARCHAR(15) NOT NULL,
    `dismissed_player_id` BIGINT NOT NULL,
    CONSTRAINT `fk_fow_innings` FOREIGN KEY (`innings_id`) REFERENCES `innings`(`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_fow_player` FOREIGN KEY (`dismissed_player_id`) REFERENCES `players`(`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 9. Admin Users Table
CREATE TABLE IF NOT EXISTS `admin_users` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `username` VARCHAR(50) NOT NULL UNIQUE,
    `password` VARCHAR(100) NOT NULL,
    `full_name` VARCHAR(100) NOT NULL,
    `role` VARCHAR(30) NOT NULL DEFAULT 'ROLE_ADMIN',
    `created_at` DATETIME NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
