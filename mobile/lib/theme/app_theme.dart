import 'package:flutter/material.dart';

class AppColors {
  static const background = Color(0xFFF0EFEB);
  static const ink = Color(0xFF191B20);
  static const muted = Color(0xFF50515E);
  static const blue = Color(0xFF205BFF);
  static const border = Color(0xFFE1DFDB);
}

final appTheme = ThemeData(
  useMaterial3: true,
  fontFamily: 'Inter',
  scaffoldBackgroundColor: AppColors.background,
  colorScheme: ColorScheme.fromSeed(
    seedColor: AppColors.blue,
    primary: AppColors.blue,
    onPrimary: Colors.white,
    surface: AppColors.background,
    onSurface: AppColors.ink,
  ),
  textTheme: const TextTheme(
    bodyMedium: TextStyle(color: AppColors.muted, fontSize: 14, height: 1.55),
    bodyLarge: TextStyle(color: AppColors.muted, fontSize: 15, height: 1.55),
    headlineLarge: TextStyle(
      color: AppColors.ink,
      fontSize: 32,
      fontWeight: FontWeight.w700,
      letterSpacing: -1,
    ),
  ),
  textButtonTheme: TextButtonThemeData(
    style: TextButton.styleFrom(
      foregroundColor: AppColors.blue,
      textStyle: const TextStyle(
        fontFamily: 'Inter',
        fontWeight: FontWeight.w600,
      ),
    ),
  ),
);
