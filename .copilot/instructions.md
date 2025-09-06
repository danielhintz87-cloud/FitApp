---
title: "FitApp: Advanced Fitness & Nutrition Platform Development Guide"
description: "Comprehensive GitHub Copilot instructions for modern Android fitness app development with AI integration"
version: "2.0"
last_updated: "2025-09-05"
---

# FitApp Android Development Guide

Always follow these instructions first and fallback to search or bash commands only when you encounter unexpected information that does not match the information provided here.

## Project Overview

FitApp is a comprehensive Android fitness tracking application built with modern technologies and inspired by market leaders like Freeletics, Nike Training Club, and other premium fitness platforms. The app combines personal fitness tracking with AI-powered coaching and community features.

**Core Technologies:**
- **Language**: Kotlin 100% with Jetpack Compose UI
- **Architecture**: Clean Architecture (MVVM + Repository pattern)
- **Database**: Room with SQLite, automated schema migrations
- **Design**: Material 3 Design System with custom theming
- **AI Integration**: Gemini + Perplexity APIs for intelligent coaching
- **Target**: Android SDK 34, minimum SDK 24, Java 17
- **Build System**: Gradle 8.14.3 with Kotlin DSL and version catalogs

## Market-Inspired Feature Strategy

FitApp draws inspiration from the most successful fitness apps in the market:

### Freeletics-Inspired Features (AI & Personalization)
- **Adaptive AI Training**: Workouts adjust based on user performance and feedback
- **Machine Learning Personalization**: 57M+ user data equivalent for intelligent recommendations
- **Bodyweight Focus**: Equipment-free workouts with progression levels (Endurance → Standard → Strength)
- **Coach+ Style AI**: Conversational AI for fitness guidance and motivation

### Nike Training Club Features (Content & Wellness)
- **Holistic Wellness**: Movement, Mindfulness, Nutrition, Rest, Connection
- **Professional Content**: HD video demonstrations and trainer guidance
- **Program-Based Training**: 4-6 week structured fitness journeys
- **Device Integration**: Apple Health, Google Fit, wearable compatibility

### Community & Social Features (Strava/Social Apps)
- **Community-Driven Challenges**: Monthly themed challenges and competitions
- **Social Feed**: Achievement sharing and peer motivation
- **Group Training**: Local and virtual workout partnerships
- **Achievement System**: Gamified progress with badges and streaks

## Core Focus Areas

**Primary Features:**
- Personal achievement tracking with advanced gamification
- AI-powered adaptive training plans and nutrition guidance
- Progress visualization with interactive charts and analytics
- Smart streak tracking for habits and long-term consistency
- Intelligent notifications for motivation and behavioral triggers
- Personal records (PR) tracking with predictive analytics
- Community features for social motivation and accountability

**Secondary Features:**
- Recipe management with cooking mode and shopping integration
- BMI tracking with weight loss program recommendations
- Enhanced analytics dashboard with AI-generated insights
- Offline functionality for workouts and meal planning

## Existing Database Architecture

The app uses a comprehensive Room database with these key entities:

### Core Entities (Already Implemented)
- **AiLog** - AI interaction history and learning data
- **SavedRecipeEntity** - User recipes with nutritional information and cooking features
- **PlanEntity** - Training plans and AI-generated workout programs
- **PersonalAchievementEntity** - Achievement tracking with gamification support
- **PersonalStreakEntity** - Habit and consistency tracking with motivation triggers
- **PersonalRecordEntity** - Personal fitness records and progression tracking
- **WeightEntity** - Weight tracking with historical analysis
- **BMIHistoryEntity** - BMI calculations and health trend analysis
- **FoodItemEntity** - Comprehensive food database with barcode integration
- **IntakeEntryEntity** - Nutrition logging with meal categorization

### New Entities to Implement
