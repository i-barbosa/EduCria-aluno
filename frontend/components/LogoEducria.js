export default function LogoEducria({ tamanho = 44 }) {
  return (
    <svg viewBox="0 0 100 100" width={tamanho} height={tamanho} aria-label="EDUCRIA">
      <path d="M18 72 L50 63 L82 72 L82 80 L50 73 L18 80 Z" fill="#FFFFFF" />
      <path d="M40 52 Q40 42 50 42 Q60 42 60 52 L60 70 L40 70 Z" fill="#FFFFFF" />
      <circle cx="50" cy="32" r="7.5" fill="#FFFFFF" />
      <path d="M40 44 L30 44 L30 55" stroke="#7ED9A6" strokeWidth="3" fill="none" strokeLinecap="round" />
      <circle cx="30" cy="57" r="5" fill="#7ED9A6" />
      <path d="M45 25 L38 18 L30 18" stroke="#B893FF" strokeWidth="3" fill="none" strokeLinecap="round" />
      <rect x="22" y="12" width="11" height="11" fill="#B893FF" />
      <path d="M60 44 L70 44 L70 55" stroke="#FFD670" strokeWidth="3" fill="none" strokeLinecap="round" />
      <circle cx="70" cy="57" r="5" fill="#FFD670" />
    </svg>
  );
}
