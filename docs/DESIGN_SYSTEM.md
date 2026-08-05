# 🎨 Rtiqa Design System (RDS) Documentation

The **Rtiqa Design System (RDS)** provides standardized, accessible, and high-performance Jetpack Compose components based on Material Design 3 guidelines with native Right-to-Left (RTL) Arabic & Left-to-Right (LTR) English support.

---

## 🌈 Color Tokens & Theming

- **Primary Colors**: Deep Royal Blue (`#1E3A8A`), Ocean Indigo (`#3B82F6`)
- **Secondary Accent**: Golden Emerald (`#10B981`), Amber Gold (`#F59E0B`)
- **Background Tokens**: Clean Surface (`#F8FAFC`), Night Slate (`#0F172A`)
- **Text Tokens**: Primary High Contrast (`#1E293B`), Medium Contrast (`#64748B`)

All colors are centralized in `core-design` and `app/ui/theme/Color.kt`.

---

## 🧱 Core RDS Components (`core-ui`)

### 1. `RdsButton`
Standardized button component supporting Primary, Secondary, Outlined, and Text variants with loading state indicators and 48dp touch targets.
```kotlin
RdsButton(
    text = "تسجيل الدخول",
    onClick = { viewModel.login() },
    isLoading = state.isLoading,
    modifier = Modifier.fillMaxWidth().testTag("login_button")
)
```

### 2. `RdsTextField`
Filled and Outlined input fields with built-in clear buttons, leading/trailing icons, error state messaging, and keyboard action handling.
```kotlin
RdsTextField(
    value = state.email,
    onValueChange = { viewModel.onEmailChanged(it) },
    label = "البريد الإلكتروني",
    errorMessage = state.emailError,
    modifier = Modifier.testTag("email_input")
)
```

### 3. `RdsCard`
Elevated card container with uniform 16.dp corner radius, soft ambient shadows, and optional click listeners.

### 4. `RdsTopAppBar`
Top navigation bar with back arrow support, action icons, page title, and seamless status bar inset integration.

---

## 📐 Layout & Accessibility Rules

- **Minimum Touch Target**: Every interactive element must have at least `48.dp` height/width.
- **RTL Support**: Use `Alignment.Start` and `Arrangement.Start` instead of `Left` and `Right`.
- **Testability**: Every interactive component must take a `Modifier` allowing `testTag("unique_tag_name")`.
