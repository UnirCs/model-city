import Link from 'next/link';
import Image from 'next/image';
import { auth0 } from '@modelcity/core/lib/auth/auth0';
import ThemeToggle from '@modelcity/core/components/molecules/ThemeToggle';
import UserMenu from '@modelcity/core/components/molecules/UserMenu';
import SettingsButton from '@modelcity/core/components/molecules/SettingsButton';
import Icon from '@modelcity/core/components/atoms/Icon';
import { getDictionary } from '@modelcity/core/lib/i18n/dictionaries';

/**
 * Organism: TopNavBar (authenticated app) — async Server Component
 * Fetches the Auth0 session on the server and passes the user to UserMenu.
 * Bar: brand · certificate · notifications · LanguageSwitcher · ThemeToggle · UserMenu
 *
 * @param {{ lang: string }} props
 */
export default async function TopNavBar({ lang }) {
  const [session, dict] = await Promise.all([
    auth0.getSession(),
    getDictionary(lang),
  ]);
  const user = session?.user ?? null;
  const accessToken = session?.tokenSet?.accessToken ?? null;

  return (
    <header className="fixed top-0 w-full z-50 bg-surface/50 backdrop-blur-md border-b border-outline-variant">
      <div className="w-full px-gutter flex justify-between items-center gap-sm h-16 md:h-20">
        {/* Brand with coat of arms */}
        <div className="flex items-center gap-sm shrink-0">
          <Image
            src="https://upload.wikimedia.org/wikipedia/commons/a/a3/Escudo_de_Aranjuez_%28Madrid%29.svg"
            alt="coat-of-arms"
            width={40}
            height={40}
            preload={true}
            className="shrink-0"
          />
          <span className="hidden md:block text-h3 text-primary tracking-tight font-semibold truncate">
            {dict.common.brand}
          </span>
        </div>

        {/* Right actions */}
        <div className="flex items-center gap-0 sm:gap-xs md:gap-sm min-w-0">
          {/* Help — always visible on mobile too */}
          <Link
            href={`/${lang}/help`}
            aria-label={dict.nav.help}
            className="flex items-center gap-xs p-xs rounded-md text-label-sm text-on-surface-variant hover:bg-surface-container hover:text-on-surface transition-colors focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-primary"
          >
            <Icon name="help_outline" size={18} />
            <span className="hidden md:inline">{dict.nav.help}</span>
          </Link>

          <SettingsButton accessToken={accessToken} />
          <ThemeToggle />

          {/* UserMenu — desktop only; on mobile/tablet the profile is in the bottom FAB nav */}
          <span className="hidden lg:contents">
            <UserMenu user={user} />
          </span>
        </div>
      </div>
    </header>
  );
}
