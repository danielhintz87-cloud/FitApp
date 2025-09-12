# Accessibility Checklist

This document provides a comprehensive checklist for manual accessibility testing using TalkBack (Android) and VoiceOver (iOS/Web). All new features and UI changes should be tested against this checklist.

## Automated Checks (CI/Lint)

The following accessibility issues are automatically checked by our CI pipeline:
- ✅ Content descriptions for images, icons, and decorative elements
- ✅ Touch target size compliance (minimum 48dp)
- ✅ Clickable elements have proper accessibility support
- ✅ Focusable elements have appropriate labels
- ✅ Compose accessibility semantics

## Manual TalkBack Testing (Android)

### Getting Started
1. **Enable TalkBack**: Settings → Accessibility → TalkBack → On
2. **Basic navigation**: Swipe right/left to move between elements
3. **Activation**: Double-tap to activate focused element
4. **Reading mode**: Two-finger single tap to start/stop continuous reading

### Core Testing Areas

#### 1. Navigation and Focus Order
- [ ] **Logical reading order**: Elements are announced in a logical sequence (left-to-right, top-to-bottom)
- [ ] **Focus order follows visual hierarchy**: Important elements are focused first
- [ ] **No focus traps**: Users can navigate to all interactive elements and back
- [ ] **Skip links work**: Users can bypass repetitive content where applicable
- [ ] **Back navigation works**: Back button/gesture properly announced and functional

#### 2. Content and Labels
- [ ] **All interactive elements have labels**: Buttons, links, form fields have clear descriptions
- [ ] **Images have alt text**: Content images have descriptive content descriptions
- [ ] **Decorative images are hidden**: Decorative elements are marked as decorative
- [ ] **Dynamic content announced**: Loading states, errors, and updates are announced
- [ ] **State information provided**: Button states (pressed, expanded, selected) are announced
- [ ] **Context provided**: Form field labels and error messages are associated properly

#### 3. Forms and Input
- [ ] **Field labels are clear**: Input purpose is obvious from label alone
- [ ] **Required fields indicated**: Required status announced
- [ ] **Error messages linked**: Errors are announced and associated with fields
- [ ] **Input format explained**: Expected format explained (e.g., date format)
- [ ] **Validation feedback provided**: Real-time validation announces changes

#### 4. Interactive Elements
- [ ] **Button purpose clear**: Button text describes action
- [ ] **Links descriptive**: Link text explains destination/purpose
- [ ] **Touch targets adequate**: Minimum 48dp touch targets
- [ ] **Double-tap activates**: All interactive elements respond to double-tap
- [ ] **Expandable content works**: Collapsible sections announce state changes

#### 5. Media and Rich Content
- [ ] **Video controls accessible**: Play/pause/volume controls work with TalkBack
- [ ] **Audio descriptions available**: For video content where applicable
- [ ] **Live regions announced**: Dynamic content updates announced appropriately
- [ ] **Progress indicators work**: Loading and progress states announced

#### 6. Visual and Temporal
- [ ] **No time limits**: Or time limits can be extended/disabled
- [ ] **No seizure-inducing content**: No rapidly flashing content
- [ ] **Animation can be reduced**: Respects system accessibility preferences
- [ ] **Sufficient contrast**: Text meets WCAG contrast requirements (4.5:1 normal, 3:1 large)

## VoiceOver Testing (iOS/Web)

### Basic Controls
- **Navigate**: Swipe right/left or use rotor
- **Activate**: Double-tap
- **Rotor**: Rotate two fingers to change navigation mode
- **Read all**: Two-finger swipe up

### Key Differences from TalkBack
- [ ] **Rotor navigation works**: Can navigate by headings, links, form controls
- [ ] **Gesture shortcuts work**: System gestures function properly
- [ ] **Speech rate adjustable**: Content readable at different speeds

## Color and Contrast Testing

### Manual Checks
- [ ] **Text contrast passes**: Use contrast checker tools (4.5:1 minimum)
- [ ] **Color not sole indicator**: Information not conveyed by color alone
- [ ] **High contrast mode works**: App functions in high contrast mode
- [ ] **Dark mode accessible**: Dark theme maintains accessibility standards

### Tools for Testing
- **Contrast checkers**: WebAIM Contrast Checker, Colour Contrast Analyser
- **Simulator tools**: iOS Accessibility Inspector, Android Accessibility Scanner

## Performance and Responsiveness

- [ ] **TalkBack responsive**: No significant delays in announcements
- [ ] **Gesture recognition reliable**: Swipes and taps register consistently
- [ ] **Battery impact minimal**: Extended TalkBack use doesn't drain battery excessively

## Testing Scenarios

### New User Experience
- [ ] **First-time user can complete key flows**: Onboarding accessible
- [ ] **Help and support accessible**: Documentation and support features work

### Complex Interactions
- [ ] **Multi-step forms work**: Wizards and complex forms navigable
- [ ] **Data entry efficient**: Text input not overly verbose
- [ ] **Error recovery possible**: Users can fix errors and continue

### Edge Cases
- [ ] **Offline functionality**: App remains accessible without network
- [ ] **Interrupted sessions**: TalkBack interruptions don't break flow
- [ ] **Device rotation**: Orientation changes maintain accessibility

## Common Issues to Watch For

### Anti-patterns
- ❌ **Clickable text without proper semantics**
- ❌ **Images without content descriptions**
- ❌ **Form fields without labels**
- ❌ **Custom controls without accessibility support**
- ❌ **Information conveyed only through color**
- ❌ **Auto-playing media without controls**

### Best Practices
- ✅ **Use platform UI components when possible**
- ✅ **Provide multiple ways to complete tasks**
- ✅ **Test with real assistive technology users**
- ✅ **Include accessibility in design reviews**

## Documentation and Reporting

### When Testing
- **Record issues**: Note specific elements and expected vs. actual behavior
- **Test environment**: Document TalkBack version, Android version, device
- **User context**: Consider different user needs and scenarios

### Reporting Format
```
**Issue**: Brief description
**Location**: Screen/component where issue occurs
**Expected**: What should happen
**Actual**: What currently happens
**Impact**: Severity for users (blocking, difficult, minor)
**Reproduction**: Steps to reproduce
```

## Testing Checklist Summary

**Before Release:**
- [ ] All automated lint checks pass
- [ ] Manual TalkBack testing completed on key user flows
- [ ] Color contrast verified
- [ ] Form interactions tested
- [ ] Dynamic content announcements verified
- [ ] Navigation flow logical and complete

**For each PR:**
- [ ] New UI elements have content descriptions
- [ ] Interactive elements work with TalkBack
- [ ] No new accessibility regressions introduced
- [ ] Complex components tested manually

Remember: **Accessibility is not just compliance—it's about creating inclusive experiences for all users.**