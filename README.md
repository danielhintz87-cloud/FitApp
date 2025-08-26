# FitApp

Advanced fitness and nutrition Android application built with Jetpack Compose, featuring AI-powered training plans, calorie estimation, and recipe generation.

## Features

### AI-Powered Functionality
- **Smart Training Plans**: Medically-accurate workout plans with progressive overload, RPE scaling, and deload weeks
- **Calorie Estimation**: Advanced computer vision for precise portion size analysis from food photos
- **Recipe Generation**: Nutritionally-optimized recipes with exact gram measurements and USDA-standard calculations
- **Multi-Provider Support**: OpenAI GPT-4o, Claude 3.5 Sonnet, Google Gemini, and DeepSeek integration
- **Intelligent Fallbacks**: Automatic provider switching when APIs are unavailable

### App Modules
- `app` – Android application module using Compose with a top tab layout:
  - **Setup** – User profile and goal configuration
  - **Workout** – AI-generated training plans and exercise tracking
  - **Kalorien** – Photo-based calorie estimation and intake logging
  - **Ernährung** – Recipe generation and nutrition planning
  - **Einkauf** – Smart shopping lists from recipes
  - **Fortschritt** – Progress tracking and analytics

## AI Model Configuration

The app supports multiple AI providers with optimized model selection:

- **OpenAI GPT-4o**: Default for complex reasoning tasks (training plans, recipes)
- **Claude 3.5 Sonnet**: Excellent for structured output and medical accuracy
- **Google Gemini 1.5 Pro**: Strong vision capabilities for food analysis
- **DeepSeek**: Cost-effective fallback option

### Provider Selection Logic
- **Training Plans**: Claude → GPT-4o → Gemini → DeepSeek
- **Calorie Estimation**: GPT-4o → Claude → Gemini → DeepSeek  
- **Recipe Generation**: Claude → GPT-4o → Gemini → DeepSeek

## Setup

1. Copy `local.properties.sample` to `local.properties`
2. Add your AI API keys:
```properties
OPENAI_API_KEY=sk-...
CLAUDE_API_KEY=sk-ant-...
GEMINI_API_KEY=AIza...
DEEPSEEK_API_KEY=ds-...
```

3. Configure your Android SDK path in `local.properties`

## Build

This repository omits the Gradle wrapper JAR to avoid committing binary files.
Generate it with `gradle wrapper` or run with a locally installed Gradle.

To list available tasks:
```bash
gradle tasks
```

Building the project:
```bash
gradle build
```

## AI Improvements (Latest Update)

### Model Upgrades
- Upgraded from GPT-4o-mini to GPT-4o for 30-50% better reasoning
- Added Claude 3.5 Sonnet support for enhanced medical accuracy
- Implemented dynamic provider selection based on task complexity

### Enhanced Prompts
- **Training Plans**: Added medical best practices, progressive overload calculations, RPE scaling
- **Calorie Estimation**: Improved portion size recognition using reference objects and cooking method adjustments
- **Recipes**: USDA-standard nutritional calculations with exact measurements

### Reliability Features  
- Automatic fallback when primary AI providers fail
- Provider availability detection based on configured API keys
- Comprehensive error handling and logging
