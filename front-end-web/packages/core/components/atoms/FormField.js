import { Children, cloneElement, isValidElement } from 'react';

/**
 * Atom: FormField
 *
 * Wrapper that groups a label, an input/select slot, an optional hint, and
 * an optional inline validation error message.
 *
 * In addition to rendering the surrounding markup, `FormField` injects
 * accessibility attributes into the **first valid child element** via
 * `React.cloneElement`:
 *
 *   - `id`                  — set to the prop `id` if the child does not declare one.
 *   - `aria-invalid="true"` — when `error` is truthy.
 *   - `aria-required="true"`+ native `required` — when `required` is `true`.
 *   - `aria-describedby`    — wired to either the error or hint paragraph,
 *                              so screen readers announce the helper text
 *                              along with the field.
 *
 * Usage:
 *   <FormField id="email" label="Email" required error={errors.email}>
 *     <input type="email" autoComplete="email" />
 *   </FormField>
 *
 * WCAG 2.2 — 1.3.1 Info and Relationships, 3.3.1 Error Identification,
 * 3.3.2 Labels or Instructions, 4.1.2 Name/Role/Value.
 *
 * @param {{
 *   id: string,
 *   label: string,
 *   hint?: string,
 *   error?: string | null,
 *   required?: boolean,
 *   children: React.ReactNode,
 * }} props
 */
export default function FormField({ id, label, hint, error, required = false, children }) {
  const errorId = error ? `${id}-error` : null;
  const hintId  = !error && hint ? `${id}-hint` : null;
  const describedBy = errorId ?? hintId ?? undefined;

  // Inject a11y props into the first valid child only. Any subsequent
  // children are passed through untouched.
  let injected = false;
  const enhancedChildren = Children.map(children, (child) => {
    if (injected || !isValidElement(child)) return child;
    injected = true;
    const existing = child.props ?? {};
    return cloneElement(child, {
      id:               existing.id ?? id,
      required:         existing.required ?? required,
      'aria-required':  existing['aria-required'] ?? (required ? 'true' : undefined),
      'aria-invalid':   existing['aria-invalid'] ?? (error ? 'true' : undefined),
      'aria-describedby': [existing['aria-describedby'], describedBy].filter(Boolean).join(' ') || undefined,
    });
  });

  return (
    <div className="flex flex-col gap-xs">
      <label htmlFor={id} className="text-label-md text-on-surface font-medium">
        {label}
        {required && (
          <span className="text-error ml-xs" aria-hidden="true">
            *
          </span>
        )}
      </label>

      {enhancedChildren}

      {error ? (
        <p id={errorId} className="text-caption text-error" role="alert">
          {error}
        </p>
      ) : hint ? (
        <p id={hintId} className="text-caption text-on-surface-variant">{hint}</p>
      ) : null}
    </div>
  );
}

