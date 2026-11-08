import 'package:flutter/material.dart';

import '../theme/app_theme.dart';
import '../widgets/app_background.dart';
import '../widgets/auth_widgets.dart';

class SignUpScreen extends StatefulWidget {
  const SignUpScreen({super.key});

  @override
  State<SignUpScreen> createState() => _SignUpScreenState();
}

class _SignUpScreenState extends State<SignUpScreen> {
  final formKey = GlobalKey<FormState>();
  final nameController = TextEditingController();
  final emailController = TextEditingController();
  final passwordController = TextEditingController();
  final confirmPasswordController = TextEditingController();

  bool get passwordsMatch =>
      passwordController.text.isNotEmpty &&
      passwordController.text == confirmPasswordController.text;

  @override
  void dispose() {
    nameController.dispose();
    emailController.dispose();
    passwordController.dispose();
    confirmPasswordController.dispose();
    super.dispose();
  }

  void createAccount() {
    if (formKey.currentState!.validate()) {
      FocusScope.of(context).unfocus();
      // TODO: connect sign up to the API.
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      body: AppBackground(
        child: SafeArea(
          child: Align(
            alignment: Alignment.topCenter,
            child: ConstrainedBox(
              constraints: const BoxConstraints(maxWidth: 440),
              child: SingleChildScrollView(
                padding: const EdgeInsets.fromLTRB(24, 4, 24, 16),
                child: AutofillGroup(
                  child: Form(
                    key: formKey,
                    child: Column(
                      crossAxisAlignment: CrossAxisAlignment.stretch,
                      children: [
                        const AuthHeader(icon: Icons.person_add_outlined),
                        const SizedBox(height: 20),
                        const Text(
                          'Create Account',
                          textAlign: TextAlign.center,
                          style: TextStyle(
                            color: AppColors.ink,
                            fontSize: 29,
                            fontWeight: FontWeight.w700,
                            letterSpacing: -0.8,
                          ),
                        ),
                        const SizedBox(height: 4),
                        const Text(
                          'Join ClubHub and connect with every club at ENSA Agadir.',
                          textAlign: TextAlign.center,
                          style: TextStyle(fontSize: 14, height: 1.55),
                        ),
                        const SizedBox(height: 20),
                        AuthTextField(
                          label: 'FULL NAME',
                          hint: 'Full Name',
                          icon: Icons.person_outline_rounded,
                          controller: nameController,
                          keyboardType: TextInputType.name,
                          autofillHints: const [AutofillHints.name],
                          validator: (value) {
                            if (value == null || value.trim().isEmpty) {
                              return 'Enter your full name.';
                            }
                            return null;
                          },
                        ),
                        const SizedBox(height: 12),
                        AuthTextField(
                          label: 'EMAIL ADDRESS',
                          hint: 'email@example.com',
                          icon: Icons.mail_outline_rounded,
                          controller: emailController,
                          keyboardType: TextInputType.emailAddress,
                          autofillHints: const [AutofillHints.email],
                          validator: (value) {
                            final email = value?.trim() ?? '';
                            if (!RegExp(
                              r'^[^\s@]+@[^\s@]+\.[^\s@]+$',
                            ).hasMatch(email)) {
                              return 'Enter a valid email address.';
                            }
                            return null;
                          },
                        ),
                        const SizedBox(height: 12),
                        AuthTextField(
                          label: 'PASSWORD',
                          hint: 'Create a password',
                          icon: Icons.lock_outline_rounded,
                          controller: passwordController,
                          isPassword: true,
                          autofillHints: const [AutofillHints.newPassword],
                          onChanged: (value) => setState(() {}),
                          validator: (value) {
                            if (value == null || value.isEmpty) {
                              return 'Create a password.';
                            }
                            return null;
                          },
                        ),
                        const SizedBox(height: 12),
                        AuthTextField(
                          label: 'CONFIRM PASSWORD',
                          hint: 'Confirm your password',
                          icon: Icons.lock_outline_rounded,
                          controller: confirmPasswordController,
                          isPassword: true,
                          showCheck: passwordsMatch,
                          textInputAction: TextInputAction.done,
                          onChanged: (value) => setState(() {}),
                          validator: (value) {
                            if (value == null || value.isEmpty) {
                              return 'Confirm your password.';
                            }
                            if (value != passwordController.text) {
                              return 'Passwords do not match.';
                            }
                            return null;
                          },
                        ),
                        const SizedBox(height: 10),
                        Wrap(
                          alignment: WrapAlignment.center,
                          crossAxisAlignment: WrapCrossAlignment.center,
                          children: [
                            const Text(
                              'By signing up you agree to our ',
                              style: TextStyle(
                                fontSize: 12,
                                color: Color(0xFF98A1B2),
                              ),
                            ),
                            _policyLink(context, 'Terms of Service'),
                            const Text(
                              ' and ',
                              style: TextStyle(
                                fontSize: 12,
                                color: Color(0xFF98A1B2),
                              ),
                            ),
                            _policyLink(context, 'Privacy Policy'),
                            const Text('.', style: TextStyle(fontSize: 12)),
                          ],
                        ),
                        const SizedBox(height: 12),
                        PrimaryButton(
                          label: 'Create Account',
                          onPressed: createAccount,
                          showArrow: true,
                        ),
                        const SizedBox(height: 12),
                        const Row(
                          children: [
                            Expanded(child: Divider(color: AppColors.border)),
                            Padding(
                              padding: EdgeInsets.symmetric(horizontal: 12),
                              child: Text(
                                'or',
                                style: TextStyle(
                                  fontSize: 12,
                                  color: Color(0xFFAEAAA3),
                                ),
                              ),
                            ),
                            Expanded(child: Divider(color: AppColors.border)),
                          ],
                        ),
                        Row(
                          mainAxisAlignment: MainAxisAlignment.center,
                          children: [
                            const Flexible(
                              child: Text(
                                'Already have an account?',
                                style: TextStyle(fontSize: 13),
                              ),
                            ),
                            TextButton(
                              onPressed: () => Navigator.pushReplacementNamed(
                                context,
                                '/login',
                              ),
                              child: const Text('Sign In'),
                            ),
                          ],
                        ),
                      ],
                    ),
                  ),
                ),
              ),
            ),
          ),
        ),
      ),
    );
  }

  Widget _policyLink(BuildContext context, String label) {
    return TextButton(
      onPressed: () {
        // TODO: open the policy page.
      },
      style: TextButton.styleFrom(
        padding: EdgeInsets.zero,
        minimumSize: const Size(0, 18),
        tapTargetSize: MaterialTapTargetSize.shrinkWrap,
        textStyle: const TextStyle(
          fontFamily: 'Inter',
          fontSize: 12,
          fontWeight: FontWeight.w600,
        ),
      ),
      child: Text(label),
    );
  }
}
