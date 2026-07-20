import ServiceCard from '@modelcity/core/components/molecules/ServiceCard';

/**
 * Organism: ServiceGrid
 * Responsive grid of municipal service cards.
 * On mobile: 2 columns with horizontal padding to keep cards away from screen edges.
 *
 * @param {{ services: Array<{id:string,title:string,description:string,icon:string,href:string}> }} props
 */
export default function ServiceGrid({ services = [] }) {
  return (
    <section aria-labelledby="services-heading">
      <div className="px-xs md:px-0 grid grid-cols-2 md:grid-cols-2 xl:grid-cols-3 gap-sm md:gap-md">
        {services.map((service) => (
          <ServiceCard key={service.id} {...service} />
        ))}
      </div>
    </section>
  );
}
