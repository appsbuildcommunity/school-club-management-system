import 'package:flutter/material.dart';

import '../theme/app_theme.dart';

class AuthHeader extends StatelessWidget {
  const AuthHeader({super.key, required this.icon});

  final IconData icon;

  @override
  Widget build(BuildContext context) {
    return Container(
      height: 148,
      decoration: BoxDecoration(
        borderRadius: BorderRadius.circular(28),
        gradient: LinearGradient(
          begin: Alignment.bottomLeft,
          end: Alignment.topRight,
          colors: [
            const Color(0xFFC4DFD9).withValues(alpha: 0.45),
            const Color(0xFF96B3EF).withValues(alpha: 0.55),
          ],
        ),
      ),
      child: Stack(
        children: [
          Positioned(
            left: 8,
            top: 6,
            child: IconButton(
              onPressed: () => Navigator.pop(context),
              tooltip: 'Back',
              style: IconButton.styleFrom(
                backgroundColor: Colors.white.withValues(alpha: 0.75),
                side: const BorderSide(color: Colors.white),
                minimumSize: const Size(44, 44),
              ),
              icon: const Icon(Icons.chevron_left, color: AppColors.ink),
            ),
          ),
          Center(
            child: Row(
              mainAxisAlignment: MainAxisAlignment.center,
              children: [
                Container(
                  width: 56,
                  height: 56,
                  decoration: BoxDecoration(
                    shape: BoxShape.circle,
                    color: AppColors.blue.withValues(alpha: 0.07),
                    border: Border.all(
                      color: Colors.white.withValues(alpha: 0.12),
                      width: 8,
                    ),
                  ),
                  child: const Icon(
                    Icons.verified_user_outlined,
                    color: AppColors.blue,
                    size: 23,
                  ),
                ),
                const SizedBox(width: 16),
                Container(
                  width: 72,
                  height: 72,
                  decoration: BoxDecoration(
                    color: AppColors.ink,
                    shape: BoxShape.circle,
                    boxShadow: [
                      BoxShadow(
                        color: AppColors.blue.withValues(alpha: 0.2),
                        blurRadius: 22,
                        offset: const Offset(0, 12),
                      ),
                    ],
                  ),
                  child: Icon(icon, color: Colors.white, size: 29),
                ),
                const SizedBox(width: 16),
                Container(
                  width: 56,
                  height: 56,
                  decoration: BoxDecoration(
                    shape: BoxShape.circle,
                    color: const Color(0xFFE8BF5F).withValues(alpha: 0.18),
                  ),
                  child: const Icon(
                    Icons.mail_outline_rounded,
                    color: Color(0xFFD39E15),
                    size: 23,
                  ),
                ),
              ],
            ),
          ),
        ],
      ),
    );
  }
}

class AuthTextField extends StatefulWidget {
  const AuthTextField({
    super.key,
    required this.label,
    required this.hint,
    required this.icon,
    required this.controller,
    this.isPassword = false,
    this.keyboardType = TextInputType.text,
    this.textInputAction = TextInputAction.next,
    this.autofillHints,
    this.validator,
    this.onChanged,
    this.showCheck = false,
  });

  final String label;
  final String hint;
  final IconData icon;
  final TextEditingController controller;
  final bool isPassword;
  final TextInputType keyboardType;
  final TextInputAction textInputAction;
  final List<String>? autofillHints;
  final String? Function(String?)? validator;
  final ValueChanged<String>? onChanged;
  final bool showCheck;

  @override
  State<AuthTextField> createState() => _AuthTextFieldState();
}

class _AuthTextFieldState extends State<AuthTextField> {
  bool hidePassword = true;

  @override
  Widget build(BuildContext context) {
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        Text(
          widget.label,
          style: const TextStyle(
            color: AppColors.muted,
            fontSize: 12,
            fontWeight: FontWeight.w600,
            height: 1.2,
          ),
        ),
        const SizedBox(height: 7),
        TextFormField(
          controller: widget.controller,
          validator: widget.validator,
          onChanged: widget.onChanged,
          keyboardType: widget.keyboardType,
          textInputAction: widget.textInputAction,
          autofillHints: widget.autofillHints,
          obscureText: widget.isPassword && hidePassword,
          autocorrect: false,
          enableSuggestions: !widget.isPassword,
          textCapitalization: widget.keyboardType == TextInputType.name
              ? TextCapitalization.words
              : TextCapitalization.none,
          style: const TextStyle(
            color: AppColors.ink,
            fontSize: 14,
            height: 1.2,
          ),
          decoration: InputDecoration(
            hintText: widget.hint,
            hintStyle: const TextStyle(color: Color(0xFFAAAAB3), fontSize: 14),
            filled: true,
            fillColor: Colors.white.withValues(alpha: 0.8),
            contentPadding: const EdgeInsets.symmetric(
              vertical: 18,
              horizontal: 16,
            ),
            prefixIcon: Icon(
              widget.icon,
              color: const Color(0xFF98A1B2),
              size: 21,
            ),
            suffixIcon: widget.showCheck
                ? const Padding(
                    padding: EdgeInsets.all(14),
                    child: CircleAvatar(
                      radius: 12,
                      backgroundColor: Color(0xFFA2D9CA),
                      child: Icon(Icons.check, size: 16, color: AppColors.ink),
                    ),
                  )
                : widget.isPassword
                ? IconButton(
                    tooltip: hidePassword ? 'Show password' : 'Hide password',
                    onPressed: () {
                      setState(() => hidePassword = !hidePassword);
                    },
                    icon: Icon(
                      hidePassword
                          ? Icons.visibility_off_outlined
                          : Icons.visibility_outlined,
                      color: const Color(0xFF98A1B2),
                      size: 22,
                    ),
                  )
                : null,
            border: OutlineInputBorder(
              borderRadius: BorderRadius.circular(17),
              borderSide: const BorderSide(color: AppColors.border),
            ),
            enabledBorder: OutlineInputBorder(
              borderRadius: BorderRadius.circular(17),
              borderSide: BorderSide(
                color: widget.showCheck ? AppColors.blue : AppColors.border,
                width: widget.showCheck ? 1.5 : 1,
              ),
            ),
            focusedBorder: OutlineInputBorder(
              borderRadius: BorderRadius.circular(17),
              borderSide: const BorderSide(color: AppColors.blue, width: 1.5),
            ),
            errorMaxLines: 2,
          ),
        ),
      ],
    );
  }
}

class PrimaryButton extends StatelessWidget {
  const PrimaryButton({
    super.key,
    required this.label,
    required this.onPressed,
    this.color = AppColors.ink,
    this.showArrow = false,
  });

  final String label;
  final VoidCallback onPressed;
  final Color color;
  final bool showArrow;

  @override
  Widget build(BuildContext context) {
    return Container(
      decoration: BoxDecoration(
        borderRadius: BorderRadius.circular(18),
        boxShadow: [
          BoxShadow(
            color: color.withValues(alpha: 0.2),
            blurRadius: 18,
            offset: const Offset(0, 10),
          ),
        ],
      ),
      child: FilledButton(
        onPressed: onPressed,
        style: FilledButton.styleFrom(
          minimumSize: const Size(double.infinity, 56),
          backgroundColor: color,
          foregroundColor: Colors.white,
          shape: RoundedRectangleBorder(
            borderRadius: BorderRadius.circular(18),
          ),
          textStyle: const TextStyle(
            fontFamily: 'Inter',
            fontSize: 15,
            fontWeight: FontWeight.w600,
          ),
        ),
        child: Row(
          mainAxisAlignment: MainAxisAlignment.center,
          children: [
            Text(label),
            if (showArrow) ...[
              const SizedBox(width: 10),
              const Icon(
                Icons.arrow_forward,
                size: 19,
                color: Color(0xFFB7B7BE),
              ),
            ],
          ],
        ),
      ),
    );
  }
}
