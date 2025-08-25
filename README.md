# FitApp

A comprehensive Android fitness application built with Jetpack Compose, featuring AI-powered workout planning, nutrition guidance, and food analysis.

## ✨ Features

### 🤖 AI Integration
- **Multi-Provider Support**: OpenAI, Google Gemini, and DeepSeek
- **Vision API**: Calorie estimation from food photos
- **Text Generation**: 12-week workout plans and recipe suggestions
- **Automatic Logging**: All AI interactions tracked in Room database

### 📱 User Interface
- **Material 3 Design**: Modern, adaptive UI with dynamic colors
- **Navigation Drawer**: Hamburger menu with full app navigation
- **Bottom Navigation**: Quick access to main features
- **Three-Dot Menu**: Additional options and settings
- **Scrollable Tabs**: Filter and organize content

### 🏋️ Fitness Features
- **12-Week Training Plans**: AI-generated personalized workouts
- **Daily Workouts**: Today's plan with alternative suggestions
- **Equipment Selection**: Customize based on available equipment
- **Intensity Levels**: Beginner to advanced options

### 🍽️ Nutrition Features
- **Food Scanning**: Photo-based calorie analysis using AI vision
- **Recipe Generation**: 10 personalized recipe suggestions
- **Dietary Preferences**: Custom filters and requirements
- **Calorie Tracking**: Visual analysis results with confidence scores

### 💾 Data Management
- **Room Database**: Local storage for AI interaction logs
- **Flow-based Updates**: Reactive UI with live data
- **Error Tracking**: Success/failure monitoring
- **Performance Metrics**: Request duration tracking

## 🏗️ Architecture

### AI Layer
- `AiCore`: Central AI processing with multi-provider support
- `AppAi`: Public API wrapper with simplified methods
- `AiConfig`: Configuration management for API keys

### Database
- `AiLog`: Entity for tracking AI interactions
- `AiLogDao`: Data access with Flow support
- `AppDatabase`: Room database configuration

### UI Components
- `MainScaffold`: Navigation structure with drawer and bottom bar
- `AiLogsScreen`: Monitor AI interactions and performance
- `FoodScanScreen`: Photo picker and vision analysis
- Specialized screens for workouts, recipes, and progress

## 🚀 Setup

1. **Clone the repository**
   ```bash
   git clone https://github.com/danielhintz87-cloud/FitApp.git
   ```

2. **Configure API Keys**
   ```bash
   cp local.properties.sample local.properties
   ```
   
   Edit `local.properties` with your API keys:
   ```properties
   OPENAI_API_KEY=your_openai_key_here
   GEMINI_API_KEY=your_gemini_key_here
   DEEPSEEK_API_KEY=your_deepseek_key_here
   ```

3. **Build and Run**
   ```bash
   ./gradlew build
   ./gradlew installDebug
   ```

## 📦 Dependencies

- **Jetpack Compose**: Modern Android UI toolkit
- **Material 3**: Latest Material Design components
- **Navigation Compose**: Type-safe navigation
- **Room**: Local database with coroutines support
- **OkHttp**: HTTP client for AI API calls
- **Kotlinx Serialization**: JSON parsing
- **Coil**: Image loading for photo preview

## 🔧 Configuration

### AI Providers
The app supports three AI providers:
- **OpenAI**: GPT-4 with vision capabilities
- **Google Gemini**: Gemini 1.5 Flash for fast responses
- **DeepSeek**: Cost-effective alternative

### Build Configuration
- Minimum SDK: 24 (Android 7.0)
- Target SDK: 34 (Android 14)
- Kotlin: 1.9.22
- Compose BOM: 2024.10.01

## 📱 Screens

1. **Plan Builder**: Create 12-week training programs
2. **Today**: View and modify daily workouts
3. **Recipes**: Generate personalized meal suggestions
4. **Food Scan**: Analyze food photos for calories
5. **Coach**: AI-powered fitness guidance
6. **AI Logs**: Monitor system performance and interactions

## 🛡️ Privacy & Security

- API keys stored securely in BuildConfig
- No user data transmitted to third parties
- Local database for sensitive information
- HTTPS-only communication with AI providers

## 🚧 Future Enhancements

- Real-time workout tracking
- Progress photos and measurements
- Social features and challenges
- Offline mode capabilities
- Wearable device integration

## 📄 License

This project is licensed under the MIT License - see the LICENSE file for details.

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch
3. Commit your changes
4. Push to the branch
5. Create a Pull Request
