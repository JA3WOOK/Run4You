import { Check } from "lucide-react";

export interface Step {
    label: string;
    sub?: string;
    time?: string;
}

interface StepperProps {
    steps: Step[];
    current: number;
    vertical?: boolean;
}

export function Stepper({ steps, current, vertical = false }: StepperProps) {
    if (vertical) {
        return (
            <div className="flex flex-col">
                {steps.map((step, i) => {
                    const done = i < current;
                    const active = i === current;
                    return (
                        <div key={i} className="flex gap-4">
                            <div className="flex flex-col items-center">
                                <div
                                    className="w-8 h-8 rounded-full flex items-center justify-center shrink-0 z-10"
                                    style={{
                                        background: done ? "#16A34A" : active ? "#2563EB" : "#F1F5F9",
                                        border: active ? "2px solid #2563EB" : done ? "none" : "2px solid #E2E8F0",
                                        boxShadow: active ? "0 0 0 4px #DBEAFE" : "none",
                                    }}
                                >
                                    {done ? (
                                        <Check size={14} color="#fff" />
                                    ) : (
                                        <span style={{ fontSize: 12, fontWeight: 700, color: active ? "#fff" : "#94A3B8" }}>{i + 1}</span>
                                    )}
                                </div>
                                {i < steps.length - 1 && (
                                    <div className="w-0.5 flex-1 my-1" style={{ background: done ? "#16A34A" : "#E2E8F0", minHeight: 32 }} />
                                )}
                            </div>
                            <div className="pb-6 pt-1">
                                <div style={{ fontSize: 13, fontWeight: active ? 600 : 500, color: active ? "#0F172A" : done ? "#64748B" : "#94A3B8" }}>
                                    {step.label}
                                </div>
                                {step.sub && <div style={{ fontSize: 11, color: "#94A3B8", marginTop: 2 }}>{step.sub}</div>}
                                {step.time && done && <div style={{ fontSize: 10, color: "#16A34A", marginTop: 2, fontWeight: 600 }}>{step.time}</div>}
                            </div>
                        </div>
                    );
                })}
            </div>
        );
    }

    return (
        <div className="flex items-center">
            {steps.map((step, i) => {
                const done = i < current;
                const active = i === current;
                return (
                    <div key={i} className="flex items-center">
                        <div className="flex flex-col items-center gap-1.5">
                            <div
                                className="w-8 h-8 rounded-full flex items-center justify-center"
                                style={{
                                    background: done ? "#16A34A" : active ? "#2563EB" : "#F1F5F9",
                                    border: active ? "2px solid #2563EB" : "none",
                                    boxShadow: active ? "0 0 0 3px #DBEAFE" : "none",
                                }}
                            >
                                {done ? <Check size={14} color="#fff" /> : (
                                    <span style={{ fontSize: 12, fontWeight: 700, color: active ? "#fff" : "#94A3B8" }}>{i + 1}</span>
                                )}
                            </div>
                            <span style={{ fontSize: 11, fontWeight: active ? 600 : 400, color: active ? "#0F172A" : done ? "#16A34A" : "#94A3B8", whiteSpace: "nowrap" }}>
                {step.label}
              </span>
                        </div>
                        {i < steps.length - 1 && (
                            <div className="h-0.5 w-16 mx-2 mb-5" style={{ background: done ? "#16A34A" : "#E2E8F0" }} />
                        )}
                    </div>
                );
            })}
        </div>
    );
}