import 'package:flutter/material.dart';

import '../theme/app_theme.dart';

class AppBackground extends StatelessWidget {
  const AppBackground({super.key, required this.child});

  final Widget child;

  @override
  Widget build(BuildContext context) {
    return Stack(
      children: [
        const Positioned.fill(
          child: DecoratedBox(
            decoration: BoxDecoration(color: AppColors.background),
          ),
        ),
        // Radial gradients keep the soft colours visible on any screen size.
        const Positioned.fill(
          child: DecoratedBox(
            decoration: BoxDecoration(
              gradient: RadialGradient(
                center: Alignment(0.8, -0.85),
                radius: 0.8,
                colors: [Color(0xFF9FB8F1), Color(0x009FB8F1)],
              ),
            ),
          ),
        ),
        const Positioned.fill(
          child: DecoratedBox(
            decoration: BoxDecoration(
              gradient: RadialGradient(
                center: Alignment(-1, -0.1),
                radius: 0.65,
                colors: [Color(0xFFEEDBB4), Color(0x00EEDBB4)],
              ),
            ),
          ),
        ),
        const Positioned.fill(
          child: DecoratedBox(
            decoration: BoxDecoration(
              gradient: RadialGradient(
                center: Alignment(0.8, 0.4),
                radius: 0.7,
                colors: [Color(0xFFC4E3D8), Color(0x00C4E3D8)],
              ),
            ),
          ),
        ),
        child,
      ],
    );
  }
}
