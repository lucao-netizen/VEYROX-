import { Check, Copy } from "lucide-react";
import React, { useState } from "react";
import ReactMarkdown from "react-markdown";
import rehypeHighlight from "rehype-highlight";
import remarkGfm from "remark-gfm";

function isSafeHref(href?: string): boolean {
  if (!href) return false;
  try {
    const parsed = new URL(href, "https://veyrox.local");
    return (
      parsed.protocol === "http:" ||
      parsed.protocol === "https:" ||
      parsed.protocol === "mailto:"
    );
  } catch {
    return false;
  }
}

interface CodeBlockProps {
  className?: string;
  children: React.ReactNode;
}

const CodeBlock: React.FC<CodeBlockProps> = ({ className, children }) => {
  const [copied, setCopied] = useState(false);
  const match = /language-([\w-]+)/.exec(className || "");
  const language = match ? match[1] : "code";

  const extractText = (node: React.ReactNode): string => {
    if (typeof node === "string") return node;
    if (Array.isArray(node)) return node.map(extractText).join("");
    if (React.isValidElement<{ children?: React.ReactNode }>(node)) {
      return extractText(node.props.children);
    }
    return "";
  };

  const handleCopy = async () => {
    const text = extractText(children).replace(/\n$/, "");
    try {
      await navigator.clipboard.writeText(text);
      setCopied(true);
      setTimeout(() => setCopied(false), 1800);
    } catch {
      // Clipboard unavailable
    }
  };

  return (
    <div className="my-3 overflow-hidden rounded-xl border border-slate-700/50 bg-[var(--vx-code-bg)]">
      <div className="flex items-center justify-between border-b border-slate-800 px-3.5 py-2 text-xs text-slate-400">
        <span className="font-mono-code uppercase tracking-wider text-[11px] text-sky-400">
          {language}
        </span>
        <button
          type="button"
          onClick={handleCopy}
          className="inline-flex items-center gap-1.5 rounded-lg px-2 py-1 text-[11px] font-medium text-slate-300 hover:bg-slate-800 hover:text-white transition-colors cursor-pointer"
        >
          {copied ? <Check className="w-3.5 h-3.5 text-emerald-400" /> : <Copy className="w-3.5 h-3.5" />}
          <span>{copied ? "Copied" : "Copy"}</span>
        </button>
      </div>
      <pre className="overflow-x-auto p-3.5 text-[13px] leading-relaxed">
        <code className={`${className || ""} font-mono-code`}>{children}</code>
      </pre>
    </div>
  );
};

export const MarkdownRenderer: React.FC<{ content: string }> = ({ content }) => {
  return (
    <div className="veyrox-markdown text-sm leading-relaxed space-y-2.5 break-words">
      <ReactMarkdown
        remarkPlugins={[remarkGfm]}
        rehypePlugins={[rehypeHighlight]}
        components={{
          h1: ({ children }) => (
            <h1 className="font-display text-lg font-bold text-[var(--vx-text)] mt-3 mb-1">
              {children}
            </h1>
          ),
          h2: ({ children }) => (
            <h2 className="font-display text-base font-bold text-[var(--vx-text)] mt-2.5 mb-1">
              {children}
            </h2>
          ),
          h3: ({ children }) => (
            <h3 className="font-display text-sm font-semibold text-[var(--vx-text)] mt-2 mb-1">
              {children}
            </h3>
          ),
          p: ({ children }) => <p className="leading-relaxed">{children}</p>,
          ul: ({ children }) => <ul className="list-disc pl-5 space-y-1">{children}</ul>,
          ol: ({ children }) => <ol className="list-decimal pl-5 space-y-1">{children}</ol>,
          blockquote: ({ children }) => (
            <blockquote className="border-l-2 border-[var(--vx-primary)] bg-[var(--vx-primary-soft)]/50 px-3 py-1.5 rounded-r-lg text-xs text-[var(--vx-text-muted)]">
              {children}
            </blockquote>
          ),
          a: ({ href, children }) =>
            isSafeHref(href) ? (
              <a
                href={href}
                target="_blank"
                rel="noopener noreferrer"
                className="text-[var(--vx-primary-bright)] underline underline-offset-2 hover:opacity-80"
              >
                {children}
              </a>
            ) : (
              <span>{children}</span>
            ),
          code: ({ className, children, ...props }) => {
            const isBlock = Boolean(className && className.includes("language-")) ||
              (typeof children === "string" && children.includes("\n"));
            if (isBlock) {
              return <CodeBlock className={className}>{children}</CodeBlock>;
            }
            return (
              <code
                {...props}
                className="font-mono-code text-[12.5px] px-1.5 py-0.5 rounded-md bg-[var(--vx-primary-soft)] text-[var(--vx-primary-bright)]"
              >
                {children}
              </code>
            );
          }
        }}
      >
        {content}
      </ReactMarkdown>
    </div>
  );
};
