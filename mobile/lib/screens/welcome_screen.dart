import 'package:flutter/material.dart';

import '../theme/app_theme.dart';
import '../widgets/app_background.dart';
import '../widgets/auth_widgets.dart';

class WelcomeScreen extends StatelessWidget {
  const WelcomeScreen({super.key});

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      body: AppBackground(
        child: SafeArea(
          child: Center(
            child: ConstrainedBox(
              constraints: const BoxConstraints(maxWidth: 440),
              child: SingleChildScrollView(
                padding: const EdgeInsets.fromLTRB(24, 24, 24, 16),
                child: Column(
                  children: [
                    Row(
                      mainAxisAlignment: MainAxisAlignment.center,
                      children: [
                        Container(
                          width: 36,
                          height: 36,
                          decoration: BoxDecoration(
                            color: AppColors.ink,
                            borderRadius: BorderRadius.circular(10),
                          ),
                          child: const Icon(
                            Icons.school_outlined,
                            color: Colors.white,
                            size: 22,
                          ),
                        ),
                        const SizedBox(width: 9),
                        const Text(
                          'ClubHub',
                          style: TextStyle(
                            color: AppColors.ink,
                            fontSize: 21,
                            fontWeight: FontWeight.w700,
                            letterSpacing: -0.6,
                          ),
                        ),
                      ],
                    ),
                    const SizedBox(height: 24),
                    const WelcomeArtwork(),
                    const SizedBox(height: 28),
                    const Text(
                      'Welcome to ClubHub',
                      textAlign: TextAlign.center,
                      style: TextStyle(
                        color: AppColors.ink,
                        fontSize: 29,
                        fontWeight: FontWeight.w700,
                        letterSpacing: -1,
                      ),
                    ),
                    const SizedBox(height: 10),
                    const Text(
                      'Your campus life, all in one place. Join clubs, '
                      'attend events, and lead your community at ENSA Agadir.',
                      textAlign: TextAlign.center,
                      style: TextStyle(
                        fontSize: 14,
                        color: AppColors.muted,
                        height: 1.6,
                      ),
                    ),
                    const SizedBox(height: 46),
                    Container(
                      padding: const EdgeInsets.all(7),
                      decoration: BoxDecoration(
                        color: Colors.white.withValues(alpha: 0.8),
                        borderRadius: BorderRadius.circular(27),
                        border: Border.all(color: Colors.white),
                      ),
                      child: Column(
                        children: [
                          PrimaryButton(
                            label: 'Get Started',
                            onPressed: () =>
                                Navigator.pushNamed(context, '/sign-up'),
                          ),
                          const SizedBox(height: 8),
                          Row(
                            children: [
                              Expanded(
                                child: TextButton(
                                  onPressed: () =>
                                      Navigator.pushNamed(context, '/login'),
                                  style: TextButton.styleFrom(
                                    minimumSize: const Size(0, 50),
                                    backgroundColor: const Color(0xFFF7F6F2),
                                    foregroundColor: AppColors.ink,
                                    shape: RoundedRectangleBorder(
                                      borderRadius: BorderRadius.circular(18),
                                    ),
                                  ),
                                  child: const Text('Sign In'),
                                ),
                              ),
                              const SizedBox(width: 12),
                              Expanded(
                                child: OutlinedButton(
                                  onPressed: () {
                                    // TODO: open the guest home screen.
                                  },
                                  style: OutlinedButton.styleFrom(
                                    minimumSize: const Size(0, 50),
                                    foregroundColor: AppColors.muted,
                                    side: const BorderSide(
                                      color: Color(0xFFD4D1CB),
                                    ),
                                    padding: const EdgeInsets.symmetric(
                                      horizontal: 8,
                                    ),
                                    shape: RoundedRectangleBorder(
                                      borderRadius: BorderRadius.circular(18),
                                    ),
                                  ),
                                  child: const Text(
                                    'Browse as Guest',
                                    textAlign: TextAlign.center,
                                  ),
                                ),
                              ),
                            ],
                          ),
                        ],
                      ),
                    ),
                  ],
                ),
              ),
            ),
          ),
        ),
      ),
    );
  }
}

class WelcomeArtwork extends StatelessWidget {
  const WelcomeArtwork({super.key});

  @override
  Widget build(BuildContext context) {
    return ClipRRect(
      borderRadius: BorderRadius.circular(36),
      child: AspectRatio(
        aspectRatio: 345 / 360,
        child: FittedBox(
          fit: BoxFit.cover,
          // Display only the illustration inside the exported welcome screen.
          child: ClipRect(
            child: Align(
              alignment: const Alignment(0, -0.439),
              widthFactor: 345 / 393,
              heightFactor: 360 / 852,
              child: Image.asset(
                'assets/images/welcome_design.png',
                width: 393,
                height: 852,
              ),
            ),
          ),
        ),
      ),
    );
  }
}
