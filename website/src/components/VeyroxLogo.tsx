import React from "react";

interface VeyroxLogoProps {
  size?: number;
  showWordmark?: boolean;
  className?: string;
}

export const VeyroxLogo: React.FC<VeyroxLogoProps> = ({
  size = 32,
  showWordmark = true,
  className = ""
}) => {
  return (
    <span className={`inline-flex items-center gap-2.5 select-none ${className}`}>
      <svg
        width={size}
        height={size}
        viewBox="0 0 64 64"
        fill="none"
        xmlns="http://www.w3.org/2000/svg"
        aria-hidden="true"
        className="shrink-0 drop-shadow-[0_0_12px_rgba(52,120,255,0.45)]"
      >
        <defs>
          <linearGradient id="veyroxLeftFacet" x1="8" y1="10" x2="32" y2="54" gradientUnits="userSpaceOnUse">
            <stop offset="0%" stopColor="#60A5FA" />
            <stop offset="100%" stopColor="#2563EB" />
          </linearGradient>
          <linearGradient id="veyroxRightFacet" x1="56" y1="10" x2="32" y2="54" gradientUnits="userSpaceOnUse">
            <stop offset="0%" stopColor="#93C5FD" />
            <stop offset="100%" stopColor="#3478FF" />
          </linearGradient>
          <linearGradient id="veyroxCore" x1="32" y1="18" x2="32" y2="44" gradientUnits="userSpaceOnUse">
            <stop offset="0%" stopColor="#E0F2FE" />
            <stop offset="100%" stopColor="#60A5FA" />
          </linearGradient>
        </defs>
        {/* Outer precision frame */}
        <rect
          x="2"
          y="2"
          width="60"
          height="60"
          rx="16"
          fill="#0B101B"
          stroke="rgba(96, 165, 250, 0.28)"
          strokeWidth="1.5"
        />
        {/* Left geometric V wing */}
        <polygon points="12,15 23,15 32,43 27,51" fill="url(#veyroxLeftFacet)" />
        {/* Right geometric V wing */}
        <polygon points="52,15 41,15 32,43 37,51" fill="url(#veyroxRightFacet)" />
        {/* Inner neural apex */}
        <polygon points="24,18 32,33 40,18 36,29 32,38 28,29" fill="url(#veyroxCore)" />
      </svg>

      {showWordmark && (
        <span className="font-display text-lg font-bold tracking-[0.2em] text-[var(--vx-text)]">
          VEYROX
        </span>
      )}
    </span>
  );
};
