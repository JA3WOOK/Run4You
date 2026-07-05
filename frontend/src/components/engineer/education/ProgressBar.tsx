// src/components/engineer/education/ProgressBar.tsx
interface Props {
  rate: number; // 0~100
  height?: number;
}

export default function ProgressBar({ rate, height = 8 }: Props) {
  const clamped = Math.min(100, Math.max(0, rate));
  return (
    <div
      style={{
        width: "100%",
        height,
        borderRadius: height / 2,
        background: "rgba(255,255,255,0.08)",
        overflow: "hidden",
      }}
    >
      <div
        style={{
          width: `${clamped}%`,
          height: "100%",
          borderRadius: height / 2,
          background: clamped >= 100 ? "#22C55E" : "var(--primary)",
          transition: "width 0.3s ease",
        }}
      />
    </div>
  );
}
