// @ts-check
// `@type` JSDoc annotations allow editor autocompletion and type checking
// (when paired with `@ts-check`).
// See: https://docusaurus.io/docs/api/docusaurus-config

import {themes as prismThemes} from 'prism-react-renderer';

// This runs in Node.js - Don't use client-side code here (browser APIs, JSX...)

/** @type {import('@docusaurus/types').Config} */
const config = {
  title: 'Model City',
  tagline: 'Developer documentation for the Model City platform',
  favicon: 'img/favicon.ico',

  // Future flags, see https://docusaurus.io/docs/api/docusaurus-config#future
  future: {
    v4: true, // Improve compatibility with the upcoming Docusaurus v4
  },

  // Production URL. Deployed on Vercel — adjust to the real project domain.
  // TODO: replace with the final Vercel domain (or a custom domain) once known.
  url: 'https://model-city-docs.vercel.app',
  // Vercel serves the site at the root, so the base path is '/'.
  baseUrl: '/',

  // GitHub source (used for the "edit this page" links below).
  organizationName: 'UnirCs',
  projectName: 'model-city',

  onBrokenLinks: 'throw',
  markdown: {
    mermaid: true,
    hooks: {
      onBrokenMarkdownLinks: 'warn',
    },
  },

  themes: ['@docusaurus/theme-mermaid'],

  // The documentation is written in English first; translations may be added
  // later as extra Docusaurus locales.
  i18n: {
    defaultLocale: 'en',
    locales: ['en'],
  },

  presets: [
    [
      'classic',
      /** @type {import('@docusaurus/preset-classic').Options} */
      ({
        docs: {
          sidebarPath: './sidebars.js',
          routeBasePath: '/', // serve the docs at the site root
          editUrl:
            'https://github.com/UnirCs/model-city/tree/master/documentation/',
        },
        blog: false, // this site is documentation-only
        theme: {
          customCss: './src/css/custom.css',
        },
      }),
    ],
  ],

  themeConfig:
    /** @type {import('@docusaurus/preset-classic').ThemeConfig} */
    ({
      image: 'img/docusaurus-social-card.jpg',
      colorMode: {
        respectPrefersColorScheme: true,
      },
      mermaid: {
        theme: { light: 'neo', dark: 'forest' },
      },
      navbar: {
        title: 'Model City',
        logo: {
          alt: 'Model City Logo',
          src: 'img/logo.svg',
        },
        items: [
          {
            type: 'docSidebar',
            sidebarId: 'docsSidebar',
            position: 'left',
            label: 'Documentation',
          },
          {
            href: 'https://github.com/UnirCs/model-city',
            label: 'GitHub',
            position: 'right',
          },
        ],
      },
      footer: {
        style: 'dark',
        links: [
          {
            title: 'Docs',
            items: [
              {label: 'Getting started', to: '/how-to-start/'},
              {label: 'Front-end', to: '/front-end/'},
              {label: 'Back-end', to: '/back-end/'},
              {label: 'Infrastructure', to: '/infrastructure/'},
            ],
          },
          {
            title: 'Platform',
            items: [
              {
                label: 'GitHub repository',
                href: 'https://github.com/UnirCs/model-city',
              },
            ],
          },
        ],
        copyright: `Copyright © ${new Date().getFullYear()} Model City. Built with Docusaurus.`,
      },
      prism: {
        theme: prismThemes.github,
        darkTheme: prismThemes.dracula,
        additionalLanguages: ['bash', 'json', 'hcl', 'nginx', 'yaml', 'java', 'sql'],
      },
    }),
};

export default config;
