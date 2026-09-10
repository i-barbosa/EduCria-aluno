import { Roboto } from "next/font/google";
import "./globals.css";

const roboto = Roboto({
  weight: ["400", "500", "700", "900"],
  subsets: ["latin"],
  variable: "--font-roboto",
});

export const metadata = {
  title: "EDUCRIA - modulo Degrau",
  description: "Praticar adaptativo com gamificacao individual",
};

export default function RootLayout({ children }) {
  return (
    <html lang="pt-BR" className={roboto.variable}>
      <body>{children}</body>
    </html>
  );
}
