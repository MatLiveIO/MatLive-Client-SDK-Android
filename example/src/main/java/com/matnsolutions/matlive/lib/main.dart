import 'package:example/screens/home_screen.dart';
import 'package:flutter/material.dart';
import 'package:matlive_client_sdk_flutter/mat_live.dart';

void main() async {
  WidgetsFlutterBinding.ensureInitialized();
  runApp(const MyApp());
}

class MyApp extends StatelessWidget {
  const MyApp({super.key});

  @override
  Widget build(BuildContext context) {
    return const MaterialApp(
      home: HomeScreen(),
    );
  }
}
